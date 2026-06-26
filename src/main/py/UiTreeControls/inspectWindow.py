from pywinauto import Desktop
from _ctypes import COMError
ACTIONABLE_TYPES = {
    "button",
    "menuitem",
    "edit",
    "checkbox",
    "radiobutton",
    "combobox",
    "tabitem",
    "treeitem",
    "listitem",
    "hyperlink"
}

def getSafeattr(info,attr,default):
    try:
        return getattr(info,attr,default)
    except COMError:
        return default
def walk(discoveryDictList,info,parent="",sendRealObjects=False):
    rectangle=getSafeattr(info,"rectangle",None)
    center=rectangle.mid_point()
    profile = {
    "name": getSafeattr(info, "name", ""),
    "control_type": getSafeattr(info, "control_type", ""),
    "automation_id": getSafeattr(info, "automation_id", ""),
    "class_name":getSafeattr(info,"class_name",""),
    "enabled":getSafeattr(info,"enabled",None),
    "visible":getSafeattr(info,"visible",None),
    "centerX":center.x,
    "centerY":center.y,
    "height":rectangle.height(),
    "width":rectangle.width(),
    "parentName":str(parent)
    }

    if sendRealObjects:
        profile["obj"]=info
    if str(profile["name"]) and str(profile["control_type"]).lower().strip() in ACTIONABLE_TYPES:
        discoveryDictList.append(profile)

    for child in info.children():
        walk(discoveryDictList,child,profile["name"],sendRealObjects=sendRealObjects)

def run(handle,sendRealObjects=False):
    # get the hwnd of the application/window
    rootInfo=Desktop(backend="uia").window(handle=handle).element_info
    discoveryDictList=[]
    walk(discoveryDictList,info=rootInfo,sendRealObjects=sendRealObjects)
    return     {
        "status":"OK",
        "list":discoveryDictList
        }
if __name__=="__main__":
    print(run(591684))
