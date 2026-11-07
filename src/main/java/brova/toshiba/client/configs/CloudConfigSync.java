package brova.toshiba.client.configs;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.MinecraftClient;

public final class CloudConfigSync {

    private static final Gson gson = new Gson();

    private static volatile String lastAppliedName = "";

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).followRedirects(Redirect.NEVER).build();

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(input -> {
        Thread thread = new Thread(input, "Toshiba-Cloud-Configs");
        thread.setDaemon(true);
        return thread;
    });

    volatile private String headerText;

    private final AtomicBoolean saving = new AtomicBoolean();

    volatile private String saveStatus = "";

    private long lastUpdateTimestamp = -1L;

    private boolean success;

    private String message = "";

    public static String getLastAppliedName() {
        return lastAppliedName;
    }

    public String getSaveStatus() {
        return this.saveStatus;
    }

    public boolean isSaving() {
        return this.saving.get();
    }

    public void saveCurrent(String text) {
        if (this.headerText == null) {
            if (this.saveStatus.isEmpty()) {
                String textValue = "Нет сессии облака. Войдите в лоадер заново.";
                brova.toshiba.client.utils.runtime.AccessGuard.write(this, textValue, (accessReceiver, accessValue) -> {
                    accessReceiver.saveStatus = accessValue;
                });
            }
        } else if (text == null || !text.matches("[\\p{L}\\p{N}_-]{1,64}")) {
            String textResult = "Название: до 64 букв, цифр, _ или -.";
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, textResult, (accessReceiver, accessValue) -> {
                accessReceiver.saveStatus = accessValue;
            });
        } else if (this.saving.compareAndSet(false, true)) {
            String textItem;
            try {
                textItem = brova.toshiba.client.utils.runtime.AccessGuard.invoke(null, new Object[] {}, (accessReceiver, accessArguments) -> ConfigManager.toJson());
            } catch (Exception failure) {
                String textEntry = "Не удалось прочитать настройки клиента.";
                brova.toshiba.client.utils.runtime.AccessGuard.write(this, textEntry, (accessReceiver, accessValue) -> {
                    accessReceiver.saveStatus = accessValue;
                });
                this.saving.set(false);
                return;
            }
            if (textItem.getBytes(StandardCharsets.UTF_8).length > 5242880) {
                this.saveStatus = "Конфиг превышает 5 МБ.";
                this.saving.set(false);
            } else {
                String textCopy = "Сохранение...";
                brova.toshiba.client.utils.runtime.AccessGuard.write(this, textCopy, (accessReceiver, accessValue) -> {
                    accessReceiver.saveStatus = accessValue;
                });
                JsonObject json = new JsonObject();
                json.addProperty("name", text);
                json.addProperty("content", textItem);
                try {
                    this.scheduledExecutorService.execute(() -> {
                        try {
                            var httpResponse = this.httpClient.send(this.createHttpRequestFromHeaderWithBuild("/api/game/configs", gson.toJson(json)), BodyHandlers.ofString());
                            if (httpResponse.statusCode() == 201) {
                                this.saveStatus = "Конфиг сохранён на сайте.";
                            } else if (httpResponse.statusCode() == 409) {
                                this.saveStatus = "Конфиг с таким названием уже есть.";
                            } else if (httpResponse.statusCode() == 413) {
                                this.saveStatus = "Лимит облака 5 МБ превышен.";
                            } else if (httpResponse.statusCode() != 401 && httpResponse.statusCode() != 403) {
                                this.saveStatus = "Не удалось сохранить конфиг.";
                            } else {
                                this.saveStatus = "Сессия истекла. Войдите заново через лоадер.";
                            }
                        } catch (Exception failureValue) {
                            this.saveStatus = "Нет соединения с сайтом.";
                        } finally {
                            this.saving.set(false);
                        }
                    });
                } catch (RuntimeException failureResult) {
                    this.saveStatus = "Облако недоступно. Перезапустите Ghost.";
                    this.saving.set(false);
                }
            }
        }
    }

    public void start() {
        String property = System.getProperty("toshiba.loader.token", "");
        System.clearProperty("toshiba.loader.token");
        if (!property.matches("[A-Za-z0-9_-]{43}")) {
            String text = "Нет сессии облака. Войдите в лоадер заново.";
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, text, (accessReceiver, accessValue) -> {
                accessReceiver.saveStatus = accessValue;
            });
        } else {
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, property, (accessReceiver, accessValue) -> {
                accessReceiver.headerText = accessValue;
            });
            this.scheduledExecutorService.scheduleWithFixedDelay(this::updateStatusCodeWithJsonNull, 0L, 2L, TimeUnit.SECONDS);
        }
    }

    public void stop() {
        this.scheduledExecutorService.shutdownNow();
        this.headerText = null;
    }

    private void updateStatusCodeWithJsonNull() {
        if (this.headerText != null) {
            try {
                HttpRequest httpRequest = this.createHttpRequestFromHeaderWithBuild("/api/game/configs/poll", "{}");
                var httpResponse = this.httpClient.send(httpRequest, BodyHandlers.ofString());
                if (httpResponse.statusCode() != 401 && httpResponse.statusCode() != 403) {
                    if (httpResponse.statusCode() == 200 && httpResponse.body().length() <= 12582912) {
                        JsonObject asJsonObject = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
                        if (asJsonObject.has("success") && asJsonObject.get("success").getAsBoolean() && asJsonObject.has("job") && !asJsonObject.get("job").isJsonNull()) {
                            JsonObject asJsonObjectValue = asJsonObject.getAsJsonObject("job");
                            long lastUpdateTimestamp = asJsonObjectValue.get("id").getAsLong();
                            if (lastUpdateTimestamp != this.lastUpdateTimestamp) {
                                this.lastUpdateTimestamp = lastUpdateTimestamp;
                                this.success = false;
                                String text = "Не удалось применить конфиг.";
                                brova.toshiba.client.utils.runtime.AccessGuard.write(this, text, (accessReceiver, accessValue) -> {
                                    accessReceiver.message = accessValue;
                                });
                                if (!asJsonObjectValue.get("content").isJsonNull() && !asJsonObjectValue.get("name").isJsonNull()) {
                                    String asString = asJsonObjectValue.get("content").getAsString();
                                    String asStringValue = asJsonObjectValue.get("name").getAsString();
                                    if (asString.getBytes(StandardCharsets.UTF_8).length <= 5242880) {
                                        CompletableFuture<ConfigResult> completableFuture = new CompletableFuture<>();
                                        MinecraftClient.getInstance().execute(() -> {
                                            try {
                                                completableFuture.complete(ConfigManager.apply(asString));
                                            } catch (Throwable failure) {
                                                completableFuture.completeExceptionally(failure);
                                            }
                                        });
                                        ConfigResult configResult = completableFuture.get(20L, TimeUnit.SECONDS);
                                        this.success = configResult.successful();
                                        this.message = brova.toshiba.client.utils.runtime.AccessGuard.invoke(configResult, new Object[] {}, (accessReceiver, accessArguments) -> ((ConfigResult) accessReceiver).message());
                                        if (this.success) {
                                            lastAppliedName = asStringValue;
                                        }
                                    } else {
                                        this.message = "Конфиг превышает 5 МБ.";
                                    }
                                } else {
                                    this.message = "Конфиг больше не существует.";
                                }
                            }
                            JsonObject json = new JsonObject();
                            json.addProperty("jobId", lastUpdateTimestamp);
                            json.addProperty("success", this.success);
                            json.addProperty("message", this.message);
                            this.httpClient.send(this.createHttpRequestFromHeaderWithBuild("/api/game/configs/ack", gson.toJson(json)), BodyHandlers.discarding());
                            return;
                        }
                        return;
                    }
                    return;
                }
                String textValue = "Сессия облака истекла. Войдите в лоадер заново.";
                brova.toshiba.client.utils.runtime.AccessGuard.write(this, textValue, (accessReceiver, accessValue) -> {
                    accessReceiver.saveStatus = accessValue;
                });
                this.stop();
                return;
            } catch (Exception failureValue) {
                System.err.println("[CloudConfigSync] " + failureValue.getMessage());
            }
        }
    }

    private HttpRequest createHttpRequestFromHeaderWithBuild(String text, String textValue) {
        return HttpRequest.newBuilder(URI.create("https://toshibadlc.space" + text)).timeout(Duration.ofSeconds(25L)).header("Authorization", "Bearer " + this.headerText).header("Content-Type", "application/json").POST(BodyPublishers.ofString(textValue)).build();
    }
}
