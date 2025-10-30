package commongameserver.network.request;

import commongameserver.network.Packet;
import commongameserver.network.PacketType;

import java.io.Serializable;

/**
 * Gói tin được gửi TỪ CLIENT -> SERVER
 * yêu cầu gửi lại danh sách người chơi đang online.
 * Gói tin này không cần chứa dữ liệu gì thêm.
 */
public class RequestOnlineListPacket extends Packet implements Serializable {

    // ID duy nhất cho việc tuần tự hóa
    private static final long serialVersionUID = 109L; 

    /**
     * Constructor.
     * Gọi constructor của lớp cha Packet để thiết lập loại gói tin.
     */
    @Override
    public PacketType getType(){
        return PacketType.REQUEST_ONLINE_LIST;
    }
    // Không cần thêm trường dữ liệu hay getters/setters
}

