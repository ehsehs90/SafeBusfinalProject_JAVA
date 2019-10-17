package server_tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

class ERunnable implements Runnable { // Thread Ŭ����
	// ������ �־���ϴ� Field�� ������ �־���ϰ� �������κ��� ��ǲ,�ƿ�ǲ ��Ʈ���� �̾Ƴ�����.
	Socket socket; // Ŭ���̾�Ʈ�� ����� ����
	BufferedReader br; // �Է��� ���� ��Ʈ��
	PrintWriter out; // ����� ���� ��Ʈ��
	// CarinfoVO car = new CarinfoVO();
	Connection conn = null;
	String carnum = null;

	public ERunnable(Socket socket) {
		super();
		this.socket = socket;
		try {

			this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			System.out.println("�Է¹�����");
			carnum = br.readLine();
			System.out.println(carnum);

			this.out = new PrintWriter(socket.getOutputStream()); // ���̵� �����ɷ� ���� �ٲ��;

//    	  	String carinfostr = carinfo(carnum);
//    	  	
//    	  	
//    	  	out.println(carinfostr);
//    	  	out.flush();
//    	  	System.out.println(out.toString());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Connection getConnection() {

		// oracle ����
		String jdbcUrl = "jdbc:oracle:thin:@70.12.115.53:1521:xe";
		String userId = "SCOTT";
		String userPw = "TIGER";
		Connection conn = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			conn = DriverManager.getConnection(jdbcUrl, userId, userPw);
			// System.out.println("���Ἲ��, ȣ�⼺��");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return conn;
	}

	public String carupdate(String carnum) throws SQLException {

		PreparedStatement pstmt0 = null;
		PreparedStatement pstmt3 = null;
		ResultSet rs = null;

		String returns = null;

		try {
			String sql0 = "SELECT A3.B1, A3.B2, A3.B3, A3.B4, A3.B5" + " FROM A2" + " join A1 ON A1.ROUTE = A2.ROUTE"
					+ " JOIN A3 ON A2.CARNUM = A3.CARNUM" + " WHERE A1.ID = ?";
			// String sql0 = "SELECT B1,B2 FROM A3 WHERE CARNUM = ?";
			// b1 =

			pstmt0 = getConnection().prepareStatement(sql0);
			// pstmt0.setString(1, carnum);
			pstmt0.setString(1, "1234");

			rs = pstmt0.executeQuery();

			System.out.println("carnum�� ? : @" + carnum);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (pstmt3 != null)
				try {
					pstmt3.close();
				} catch (SQLException ex) {
				}
			if (pstmt0 != null)
				try {
					pstmt0.close();
				} catch (SQLException ex) {
				}
			if (getConnection() != null)
				try {
					getConnection().close();
				} catch (SQLException ex) {
				}
		}

		return returns;
	}

	public String carinfo(String carnum) throws SQLException {

		PreparedStatement pstmt0 = null;
		PreparedStatement pstmt3 = null;
		ResultSet rs = null;
		JSONArray ja = new JSONArray();

		String returns = null;

		try {
			String sql0 = "SELECT A3.B1, A3.B2, A3.B3, A3.B4, A3.B5" + " FROM A2" + " join A1 ON A1.ROUTE = A2.ROUTE"
					+ " JOIN A3 ON A2.CARNUM = A3.CARNUM" + " WHERE A1.ID = ?";
			// String sql0 = "SELECT B1,B2 FROM A3 WHERE CARNUM = ?";
			// b1 =

			pstmt0 = getConnection().prepareStatement(sql0);
			// pstmt0.setString(1, carnum);
			pstmt0.setString(1, "1234");

			rs = pstmt0.executeQuery();

			System.out.println("carnum�� ? : @" + carnum);

			while (rs.next()) {
				JSONObject jo = new JSONObject();
				jo.put("B1", rs.getString("B1"));
				jo.put("B2", rs.getString("B2"));
				jo.put("B3", rs.getString("B3"));
				jo.put("B4", rs.getString("B4"));
				jo.put("B5", rs.getString("B5"));
				ja.add(jo);
			}

			returns = ja.toJSONString();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt3 != null)
				try {
					pstmt3.close();
				} catch (SQLException ex) {
				}
			if (pstmt0 != null)
				try {
					pstmt0.close();
				} catch (SQLException ex) {
				}
			if (getConnection() != null)
				try {
					getConnection().close();
				} catch (SQLException ex) {
				}
		}

		return returns;
	}

	@Override
	public void run() {
		// Ŭ���̾�Ʈ�� echoó�� ����
		// Ŭ���̾�Ʈ�� ���ڿ��� ������ �ش� ���ڿ��� �޾Ƽ� �ٽ� Ŭ���̾�Ʈ���� ����.
		// �ѹ��ϰ� �����ϴ°� �ƴ϶� Ŭ���̾�Ʈ�� "/EXTI"��� ���ڿ���
		// ���� ������ ����.
		String line = "";
		try {
			for (int i = 0; i < 1; i++) { // ������ ������ ������? ��� �Ǵ��� �ϰ� ��

				Thread.sleep(300);
				// ��񰡼� �����Ϳ�
				// �����°� ����..

				String carinfostr = carinfo(carnum);

				out.println(carinfostr);
				out.flush();
				System.out.println(out.toString());
			}

//         while((line = br.readLine()) != null) { // ������ ������ ������? ��� �Ǵ��� �ϰ� ��
//            if(line.equals("/EXIT/")) {
//               break; // ���� ������ loop�� Ż��!
//            }
//            else {
//               out.println(line);
//               out.flush();
//            }
//         }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

public class TCPserver extends Application {

	TextArea textarea; // �޽��� â �뵵�� ���
	Button startbtn, closebtn; // ���� ����, ���� ���� ��ư
	ExecutorService executorservice = Executors.newCachedThreadPool(); // Cached : �ɵ������� �þ��� �پ���.
	// ServerSocket server = new ServerSocket(7777);
	// Ŭ���̾�Ʈ�� ������ �޾Ƶ��̴� ���� ����. but ����ó���� �ʿ��ϱ� ������ �ʵ忡�� ���� �����ϱ� ���Ѵ�. ���� ���� ���ְ�
	// �Ʒ� �����Ҷ� ����ó���� �ϸ鼭 �����Ѵ�.
	ServerSocket server;

	private void printMsg(String msg) {
		Platform.runLater(() -> {
			textarea.appendText(msg + "\n");
		});
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// ȭ�鱸���ؼ� window ���� �ڵ�
		// ȭ��⺻ layout�� ���� => ȭ���� ���������߾�(5���� ����)���� �и�
		BorderPane root = new BorderPane();
		// BorderPane�� ũ�⸦ ���� => ȭ�鿡 ���� window�� ũ�� ����
		root.setPrefSize(700, 500);

		// Component �����ؼ� BorderPane�� ����
		textarea = new TextArea();
		root.setCenter(textarea);

		startbtn = new Button("Echo ���� ����!!");
		startbtn.setPrefSize(250, 50);
		startbtn.setOnAction(t -> {
			// ��ư���� Action�� �߻�(Ŭ��)���� �� ȣ��!
			// ���� ���α׷� ���� ��ư
			// Ŭ���̾�Ʈ ������ ��ٷ���! => ������ �Ǹ� Thread�� �ϳ� ����
			// => Thread�� �����ؼ� Ŭ���̾�Ʈ�� Thread�� ����ϵ��� ������
			// ������ �ٽ� �ٸ� Ŭ���̾�Ʈ�� ������ ��ٷ���!

			Runnable runnable = () -> {
				try {
					server = new ServerSocket(8090);
					int flag = 0;

					printMsg("Echo Server ���� 111!! ");

					// Socket s = server.accept(); // ���� ��� �κ�. ���ٸ� ������ �Ѹ��� Ŭ���̾�Ʈ�� ����
					while (true) {
						printMsg("Ŭ���̾�Ʈ ���� ���");
						Socket s = server.accept(); // ���� ��� �κ�. �������� ������ ���� ���� ������ �̿�
						// accept ������ javaFX�� ����. �׷��� Thread�� ������ ��������
						System.out.println(s);
						printMsg("Ŭ���̾�Ʈ ���� ����");

						ERunnable r = new ERunnable(s); // Ŭ���̾�Ʈ�� ���������� Thread����� �����ؿ�!
						executorservice.execute(r); // thread ����
						// Thread t2 = new Thread(r);

					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			};
			executorservice.execute(runnable);
		});

		closebtn = new Button("Echo ���� ���� !!");
		closebtn.setPrefSize(250, 50);
		closebtn.setOnAction(t -> {
			// ��ư���� Action�� �߻�(Ŭ��)���� �� ȣ��!
			// ���ӹ�ư

		});

		FlowPane flowpane = new FlowPane();
		flowpane.setPrefSize(700, 50);
		// flowpane�� ��ư�� �÷���!
		flowpane.getChildren().add(startbtn);
		flowpane.getChildren().add(closebtn);
		root.setBottom(flowpane);

		// Scene ��ü�� �ʿ��ؿ�.
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Thread �����Դϴ�.");
		primaryStage.show();
	}

	public static void main(String[] args) {

		launch();
	}
}