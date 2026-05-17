"""
AI 소비 분류 모듈
거래 설명(memo) 텍스트를 분석하여 카테고리를 자동 분류한다.
규칙 기반(Rule-based) 분류를 기본으로 하며, 향후 ML 모델로 확장 가능하다.
"""

from flask import Flask, request, jsonify
import re
import logging

logging.basicConfig(level=logging.INFO, format='%(asctime)s %(levelname)s %(message)s')
logger = logging.getLogger(__name__)

app = Flask(__name__)

# ── 규칙 기반 카테고리 분류 사전 ─────────────────────────────────────────────
CATEGORY_RULES: dict[str, list[str]] = {
    "식비": [
        "맥도날드", "버거킹", "롯데리아", "kfc", "써브웨이", "맘스터치",
        "편의점", "cu", "gs25", "세븐일레븐", "이마트24",
        "마트", "이마트", "홈플러스", "롯데마트", "코스트코",
        "식당", "음식점", "분식", "김밥", "떡볶이", "순대",
        "치킨", "피자", "족발", "보쌈", "삼겹살", "갈비",
        "배달", "배민", "요기요", "쿠팡이츠", "땡겨요",
        "식료품", "반찬", "과일", "채소",
    ],
    "카페/음료": [
        "스타벅스", "이디야", "카페", "커피", "아메리카노", "라떼",
        "투썸", "할리스", "폴바셋", "블루보틀", "엔제리너스",
        "공차", "빽다방", "메가커피", "더벤티",
        "음료", "주스", "버블티", "스무디",
    ],
    "교통": [
        "지하철", "버스", "택시", "카카오택시", "우버", "타다",
        "기차", "ktx", "srt", "무궁화",
        "주유", "sk에너지", "gs칼텍스", "현대오일뱅크", "s-oil",
        "주차", "하이패스", "톨게이트", "통행료",
        "쏘카", "그린카", "카쉐어링",
        "따릉이", "씽씽", "킥보드",
    ],
    "쇼핑": [
        "쿠팡", "11번가", "gmarket", "옥션", "티몬", "위메프",
        "네이버쇼핑", "카카오쇼핑",
        "백화점", "현대백화점", "신세계", "롯데백화점",
        "다이소", "무신사", "29cm", "musinsa",
        "올리브영", "드럭스토어",
        "의류", "옷", "신발", "가방", "악세사리",
        "전자제품", "삼성", "lg", "애플", "아이폰",
    ],
    "의료/건강": [
        "병원", "의원", "클리닉", "내과", "외과", "치과", "안과", "피부과",
        "약국", "약", "의약품",
        "헬스", "헬스장", "pt", "요가", "필라테스", "수영",
        "건강검진", "검진", "혈액검사",
    ],
    "주거": [
        "월세", "관리비", "전기요금", "가스요금", "수도요금",
        "통신비", "핸드폰", "인터넷", "kt", "skt", "lg유플러스",
        "청소", "세탁", "수리",
    ],
    "여가/오락": [
        "영화", "cgv", "메가박스", "롯데시네마",
        "게임", "스팀", "플레이스테이션", "닌텐도",
        "여행", "호텔", "숙박", "에어비앤비", "야놀자", "여기어때",
        "항공", "대한항공", "아시아나", "제주항공",
        "공연", "뮤지컬", "콘서트", "전시",
        "취미", "독서실", "만화방",
    ],
    "교육": [
        "학원", "과외", "교습",
        "교재", "책", "도서", "yes24", "알라딘", "교보문고",
        "강의", "인프런", "유데미", "coursera", "클래스101",
        "토익", "토플", "수능", "자격증",
    ],
    "금융": [
        "보험", "생명보험", "자동차보험",
        "이자", "대출", "원금",
        "펀드", "주식", "etf", "코인",
        "수수료", "이체수수료",
    ],
    "수입": [
        "급여", "월급", "알바", "아르바이트",
        "용돈", "입금", "송금",
        "이자수익", "배당",
        "판매수익", "환급", "환불", "캐시백",
    ],
}

def classify_by_rules(memo: str, tx_type: str) -> tuple[str, float]:
    """규칙 기반 카테고리 분류. (카테고리명, 신뢰도) 반환."""
    if not memo:
        return ("수입" if tx_type == "INCOME" else "기타"), 0.5

    memo_lower = memo.lower()

    # 수입 타입은 수입 카테고리 우선 검사
    if tx_type == "INCOME":
        for keyword in CATEGORY_RULES["수입"]:
            if keyword in memo_lower:
                return "수입", 0.95

    for category, keywords in CATEGORY_RULES.items():
        if category == "수입" and tx_type != "INCOME":
            continue
        for keyword in keywords:
            if keyword in memo_lower:
                return category, 0.9

    # 기본값
    return ("수입" if tx_type == "INCOME" else "기타"), 0.3


@app.route("/classify", methods=["POST"])
def classify():
    """거래 분류 엔드포인트."""
    data = request.get_json(force=True)
    memo   = data.get("memo", "")
    amount = data.get("amount", 0)
    tx_type = data.get("type", "EXPENSE").upper()

    category, confidence = classify_by_rules(memo, tx_type)

    logger.info(f"분류 완료: memo='{memo}' → category='{category}' (confidence={confidence:.2f})")

    return jsonify({
        "category":   category,
        "confidence": confidence,
        "alternatives": [],
    })


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=False)
