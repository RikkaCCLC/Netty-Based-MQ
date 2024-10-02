import com.msb.kmq.sotrage.MmapFile;

public class TestMmap {
    public static void main(String[] args) {
        MmapFile mmapFile = new MmapFile("C:\\Users\\Administrator\\Desktop\\lijin.jpg",1024*1024);
        System.out.println(mmapFile.getByteBuffer());
    }
}
