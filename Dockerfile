FROM ollama/ollama:latest

RUN ollama pull llama3.2

CMD ["serve"]