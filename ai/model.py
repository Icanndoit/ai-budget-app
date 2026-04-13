# 소비 분류 모델 (간단 버전)

def classify_expense(text):
    if "카페" in text or "커피" in text:
        return "식비"
    elif "버스" in text or "지하철" in text:
        return "교통"
    elif "쇼핑" in text:
        return "쇼핑"
    else:
        return "기타"

# 테스트
if __name__ == "__main__":
    print(classify_expense("스타벅스 커피"))
