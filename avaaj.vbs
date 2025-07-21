Dim message, sapi
  message = "Welcome"
 Set sapi = CreateObject("sapi.spvoice")
sapi.Speak message