import win32gui,win32con
import time
from pywinauto import Desktop
def win32Focus(handle):
    win=Desktop(backend="win32").window(handle=handle)
    for a in range(3):
        win.set_focus()
        time.sleep(0.1)
        if handle == win32gui.GetForegroundWindow():
            return True
    return False
def UIAFocus(handle):
    win=Desktop(backend="uia").window(handle=handle)
    for a in range(3):
        win.set_focus()
        time.sleep(0.1)
        if handle == win32gui.GetForegroundWindow():
            return True
    return False
def run(handle):
    #handle is hwnd of the application
    #un-minimize this
    if win32gui.IsIconic(handle):
        win32gui.ShowWindow(handle,win32con.SW_RESTORE)
        time.sleep(0.05)
    #Now, we will be creating object first for win32 and try focus handling (this is fast and works for most apps)
    # UIA is tried only for specific cases where Win32 is restricted by OS from managing the Focus
    # and UIA may sliently by-pass those rules and "request" for focus shift
    if win32Focus(handle):
        return {"status":"OK","method":"Win32"}
    if UIAFocus(handle):
        return {"status":"OK","method":"UIA"}
    return {
        "status":"error",
        "error":f"Failed to transfer focus to the app with hwnd {handle}"
        }
