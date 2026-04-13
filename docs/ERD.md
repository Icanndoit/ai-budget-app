# 📊 ERD 설계

## User
- user_id (PK)
- name

## Transaction
- transaction_id (PK)
- user_id (FK)
- amount
- category
- date

## Budget
- budget_id (PK)
- user_id (FK)
- limit_amount
