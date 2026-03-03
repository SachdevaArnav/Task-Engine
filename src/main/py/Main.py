from WindowControls import focusWindow, listWindows
import pythoncom,sys,json,traceback,struct
# This COINIT_MULTITHREADED we are doing for robustness from OS and libraray evolution
# Only thing need to be taken care is to CoUnintialize at SHUTDOWN
ATOM_REGISTRY={
"FOCUS_WINDOW":focusWindow.run,
"LIST_WINDOWS":listWindows.run
}
def command_input():
    raw_len=sys.stdin.buffer.read(4) #read
    if not raw_len:
        return None
    length=struct.unpack(">I",raw_len)[0]
    data=sys.stdin.buffer.read(length)
    return json.loads(data)
def response(obj):
    data=json.dumps(obj).encode('utf-8')
    length=struct.pack(">I",len(data))
    sys.stdout.buffer.write(length)
    sys.stdout.buffer.write(data)
    sys.stdout.buffer.flush()
def main():
    pythoncom.CoInitializeEx(pythoncom.COINIT_MULTITHREADED)
    # sys.stderr.write("PY: sending Ready\n")
    sys.stderr.flush()
    response({"status":"Ready"})
    try:
        while True:
            cmd=command_input()
            if cmd is None:
                break
            if cmd.get("cmd")=="SHUTDOWN":
                response({"status":"Shutdown"})
                break
            elif cmd.get("cmd")=="EXECUTE":
                atom=cmd.get("atom")
                args=cmd.get("args") or {}
                if atom not in ATOM_REGISTRY:
                    response({
                        "status":"error",
                        "error": f"Unknown atom {atom}"
                    })
                    continue
                try:
                    result=ATOM_REGISTRY[atom](**args)
                    response(result)
                except Exception as e:
                    response({
                        "status":"error",
                        "error": str(e),
                        "traceback":traceback.format_exc()
                    })
            else:
                response(
                    {
                        "status":"error",
                        "error":f"UnKnown cmd {cmd}"
                    }
                )
    finally:
        pythoncom.CoUninitialize()


if __name__=="__main__":
    main()
