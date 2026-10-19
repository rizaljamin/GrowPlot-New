FROM python:3.12-slim
WORKDIR /app
COPY vision_server.py /app/vision_server.py
ENV PYTHONUNBUFFERED=1 PORT=8080
EXPOSE 8080
USER 65532:65532
CMD ["python","vision_server.py"]
