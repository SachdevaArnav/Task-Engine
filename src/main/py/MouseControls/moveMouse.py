from win32api import SetCursorPos
from win32gui import GetCursorPos
import time
def run(x,y):
    lasterror=""
    for _ in range(2):
        try:
            SetCursorPos((x,y))
            time.sleep(0.01)
            if GetCursorPos()==(x,y):
                return {
                    "status":"OK"
                }
        except Exception as e:
            lasterror=str(e)
    return {
        "status":"error",
        "error":f"Failed to move the cursor{lasterror}"
    }
