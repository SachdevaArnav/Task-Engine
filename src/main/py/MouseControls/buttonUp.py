import ctypes,time
PUL=ctypes.POINTER(ctypes.c_ulong)
kernel32 = ctypes.WinDLL('kernel32', use_last_error=True)
class MOUSEINPUT(ctypes.Structure):
    _fields_ = [
        ("dx", ctypes.c_long),
        ("dy", ctypes.c_long),
        ("mouseData", ctypes.c_ulong),
        ("dwFlags", ctypes.c_ulong),
        ("time", ctypes.c_ulong),
        ("dwExtraInfo", PUL)
    ]
class INPUT(ctypes.Structure):
    _fields_ = [("type", ctypes.c_ulong), ("mi", MOUSEINPUT)]

SendInput=ctypes.windll.user32.SendInput

def run(button="left"):
    if button=="left":
        buttonCode=0x0004 #Left-up
    elif button=="right":
        buttonCode=0x0010 #Right-up
    else:
        return {
            "status":"error",
            "error":f"Unknwon mouse button requested {button}"
        }
    lastError=""
    for _ in range(2):
        try:
            inp = INPUT(0, MOUSEINPUT(0, 0, 0, buttonCode, 0, None))
            result=SendInput(1, ctypes.byref(inp), ctypes.sizeof(inp))
            # result tells numbers of successful events (so failure would be anything less than n (here 1))
            if result==1:
                return {
                    "status":"OK"
                }
            lastError=str(ctypes.WinError(ctypes.get_last_error()))
        except Exception as e:
            lastError=str(e)
        time.sleep(0.005)
    return {
        "status":"error",
        "error":f"Failure in Sending button action {lastError}"
    }
