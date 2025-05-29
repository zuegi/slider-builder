# slide-builder

## Docker
```shell
docker compose up -d 
```


## Q&A
### Warum pulled das mein docker-compose.yaml so langsam?
Das langsame docker-compose up liegt daran, dass im Dockerfile beim Start des Containers der Befehl ollama pull llama3.2 ausgeführt wird. Das Modell ist mehrere GB groß und wird jedes Mal beim Starten des Containers heruntergeladen, falls es noch nicht im Volume liegt.

Gründe für die Langsamkeit:

Das Modell muss beim ersten Start komplett aus dem Internet geladen werden.
Die Download-Geschwindigkeit hängt von deiner Internetverbindung und der Auslastung des Ollama-Servers ab.
Das Volume ollama_data ist leer, wenn du es noch nie verwendet hast.
Lösung:
Das Modell wird nach dem ersten Download im Docker-Volume gespeichert (ollama_data). Beim nächsten Start ist es sofort verfügbar und muss nicht erneut geladen werden.
Beim ersten Start ist die Wartezeit normal.

### slide-builder lässt sich nicht starten.
Überprüfe die Memory Settings im Docker Container

```shell
curl http://localhost:11434/api/chat -d '{"model":"llama3.2","messages":[{"role":"user","content":"Hallo"}]}' -H "Content-Type: application/json"
{"error":"model requires more system memory (3.4 GiB) than is available (1.0 GiB)"}%                                                            ```
```

Die Fehlermeldung zeigt, dass dein System (bzw. der Docker-Container) zu wenig RAM für das Modell llama3.2 hat. Das Modell benötigt mindestens 3,4 GB RAM, aber es stehen nur 1,0 GB zur Verfügung.

##### Lösung:
Weise dem Docker-Container mehr RAM zu.
Bei Docker Desktop (z. B. auf macOS) kannst du unter „Settings → Resources“ den Arbeitsspeicher erhöhen.
Starte danach den Container neu.
Erst wenn genug RAM verfügbar ist, liefert die API eine gültige Antwort mit den erwarteten Feldern.


## Model
Im ollama Docker Container führe folgenden Befehl aus.
llama3.2 sollte gelistet sein
```
# ollama list
NAME               ID              SIZE      MODIFIED       
llama3.2:latest    a80c4f17acd5    2.0 GB    58 seconds ago    
```
Ist das llama3.2 Modell nicht gelistet, dann führe folgenden Befehl aus
```
ollama pull llama3.2
```