# 📌 API 명세서

## Base URL
http://localhost:8080/api

---

## 1. 거래 API

### 거래 조회
GET /transactions

Response:
[
  {
    "id": 1,
    "amount": 10000,
    "category": "식비"
  }
]

---

### 거래 추가
POST /transactions

Body:
{
  "amount": 5000,
  "category": "교통"
}

---

### 거래 삭제
DELETE /transactions/{id}

---

## 2. 예산 API

### 예산 설정
POST /budget

Body:
{
  "limit": 300000
}

---

### 예산 조회
GET /budget

---

## 3. AI API

### 소비 분류
POST /ai/classify

Body:
{
  "text": "스타벅스 커피"
}

Response:
{
  "category": "식비"
}
