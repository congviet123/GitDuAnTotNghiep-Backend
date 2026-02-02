package poly.edu.config;

public class VuTestPush {
	  String rootPath = System.getProperty("user.dir");
      
      // 2. Chuyển đổi dấu gạch chéo ngược (\) thành xuôi (/) để đúng chuẩn URL
      String cleanRootPath = rootPath.replace("\\", "/");
      
      // 3. Ghép chuỗi để trỏ vào thư mục src
      String uploadPath = cleanRootPath + "/src/main/resources/static/imgs/";

}
