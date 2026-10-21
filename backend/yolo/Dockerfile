FROM python:3.12-slim
WORKDIR /app
COPY requirements.txt /app/requirements.txt
RUN pip install --no-cache-dir -r /app/requirements.txt
COPY yolo_server.py /app/yolo_server.py
ENV PYTHONUNBUFFERED=1 PORT=8080 YOLO_MODEL=yolov8s-world.pt
EXPOSE 8080
CMD ["uvicorn", "yolo_server:app", "--host", "0.0.0.0", "--port", "8080", "--timeout-keep-alive", "120"]
