Option Explicit
Dim shell, folder
Set shell = CreateObject("WScript.Shell")
folder = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName)
shell.CurrentDirectory = folder
shell.Run "cmd.exe /d /c .\run-client.bat --background > .\run-client.log 2>&1", 0, False
