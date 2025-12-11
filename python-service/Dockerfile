FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

COPY . .

EXPOSE 5000

# 使用环境变量配置服务
ENV HOST=0.0.0.0
ENV PORT=5000
ENV DEBUG=False

# 允许通过环境变量覆盖默认配置
CMD ["python", "app.py"]