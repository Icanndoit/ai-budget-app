import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    // 거래 조회
    @GetMapping
    public List<Map<String, Object>> getTransactions() {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> t1 = new HashMap<>();
        t1.put("id", 1);
        t1.put("amount", 10000);
        t1.put("category", "식비");

        list.add(t1);
        return list;
    }

    // 거래 추가
    @PostMapping
    public String addTransaction(@RequestBody Map<String, Object> data) {
        return "거래 추가 완료: " + data.toString();
    }

    // 거래 삭제
    @DeleteMapping("/{id}")
    public String deleteTransaction(@PathVariable int id) {
        return "거래 삭제 완료: " + id;
    }
}
