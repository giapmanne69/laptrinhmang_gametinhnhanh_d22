package servergameserver.service;

import commongameserver.model.User;
import servergameserver.db.UserDao;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lớp Singleton, chịu trách nhiệm xử lý logic liên quan đến
 * Bảng xếp hạng.
 */
public class LeaderboardService {

    // --- Singleton Pattern ---
    private static final LeaderboardService instance = new LeaderboardService();

    private LeaderboardService() {
    }

    public static LeaderboardService getInstance() {
        return instance;
    }

    // --- Dependencies ---
    private final UserDao userDao = UserDao.getInstance();

    /**
     * Lấy danh sách bảng xếp hạng đã được sắp xếp.
     * Tiêu chí sắp xếp:
     * 1. Tổng số điểm (giảm dần).
     * 2. Tổng số trận thắng (giảm dần).
     *
     * @return Một danh sách (List) các User đã được sắp xếp.
     */
    public List<User> getLeaderboard() {
        System.out.println("LeaderboardService: Lấy dữ liệu bảng xếp hạng...");

        // 1. Lấy tất cả người dùng từ CSDL
        // (Giả sử UserDao có phương thức getAllUsersSorted()
        //  để tối ưu hóa. Nếu không, chúng ta sẽ tự sắp xếp.)

        // 2. Lấy tất cả người dùng
        List<User> allUsers = userDao.getAllUsers();
        if (allUsers == null) {
            System.err.println("LeaderboardService: Không thể lấy danh sách người dùng từ DAO.");
            return List.of(); // Trả về danh sách rỗng
        }

        // 3. Định nghĩa bộ so sánh (Comparator)
        // So sánh theo 'totalScore' giảm dần
        Comparator<User> byScore = Comparator.comparingInt(User::getTotalScore).reversed();
        // So sánh theo 'totalWins' giảm dần
        Comparator<User> byWins = Comparator.comparingInt(User::getTotalWins).reversed();

        // 4. Sắp xếp danh sách
        // .thenComparing() sẽ được dùng nếu 'byScore' bằng nhau
        List<User> sortedList = allUsers.stream()
                .sorted(byScore.thenComparing(byWins))
                .collect(Collectors.toList());

        System.out.println("LeaderboardService: Trả về " + sortedList.size() + " người dùng đã sắp xếp.");

        // 5. Trả về danh sách đã sắp xếp
        return sortedList;
    }
}
