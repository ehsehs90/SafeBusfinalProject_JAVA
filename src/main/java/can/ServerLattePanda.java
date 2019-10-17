package can;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

class ERunnable implements Runnable { // Thread 클래스
	// 가지고 있어야하는 Field는 소켓이 있어야하고 소켓으로부터 인풋,아웃풋 스트림을 뽑아내야함.
	Socket socket; // 클라이언트와 연결된 소켓
	BufferedReader br; // 입력을 위한 스트림
	PrintWriter out; // 출력을 위한 스트림
	// CarinfoVO car = new CarinfoVO();
	Connection conn = null;
	String carnum = null;

	public ERunnable(Socket socket) {
		super();
		this.socket = socket;
		try {

			this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			System.out.println("입력받은값");
			carnum = br.readLine();
			System.out.println(carnum);

			this.out = new PrintWriter(socket.getOutputStream()); // 아이디 받은걸로 쓰게 바꿔라;

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

		// oracle 계정
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
			// System.out.println("연결성공, 호출성공");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return conn;
	}

	public String carinfo(String carnum) throws SQLException {

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

			System.out.println("carnum는 ? : @" + carnum);
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
		// 클라이언트와 echo처리 구현
		// 클라이언트가 문자열을 보내면 해당 문자열을 받아서 다시 클라이언트에게 전달.
		// 한번하고 종료하는게 아니라 클라이언트가 "/EXTI"라는 문자열을
		// 보낼 때까지 지속.
		String line = "";
		try {
			for (int i = 0; i < 2; i++) { // 접속을 강제로 끊었네? 라는 판단을 하게 함

				Thread.sleep(300);
				// 디비가서 가져와요
				// 가져온거 쏴요..

				String carinfostr = carinfo(carnum);

				out.println(carinfostr);
				out.flush();
				System.out.println(out.toString());
			}

//         while((line = br.readLine()) != null) { // 접속을 강제로 끊었네? 라는 판단을 하게 함
//            if(line.equals("/EXIT/")) {
//               break; // 가장 근접한 loop를 탈출!
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
public class ServerLattePanda extends Application{

	// 메시지 창 (받은 메시지를 보여주는 역할)
	TextArea textarea;

	// ServerLattePanda 포트번호 지정
	String portName = "COM10";
	
	// 연결 버튼 (COM 포트 연결 버튼) -> 메시지가 들어오는지 기다렸다가 데이터가 들어오면 받는다.
	Button connBtn, sendBtn;

	// 사용할 COM 포트를 지정하기 위해서 필요
	private CommPortIdentifier portIdentifier;

	// 만약 COM 포트를 사용할 수 있고, 해당 포트를 open 하면 COM 포트 객체를 획득
	private CommPort commPort;

	private SerialPort serialPort;

	// Byte 계열로 입출력을 한다.
	private BufferedInputStream bis;
	private OutputStream out;

	public static void main(String[] args) {
		launch();
	}

	ServerSocket server;
	ExecutorService executorservice = Executors.newCachedThreadPool();
	
	// SerialPort에서 발생하는 이벤트를 처리하기 위한 클래스
	class MyPortListener implements SerialPortEventListener{

		long mNow;
	       Date mDate;
	       SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMddhhmmss");
	       
	      private String getTime() {
	           mNow = System.currentTimeMillis();
	           mDate = new Date(mNow);
	           return mFormat.format(mDate);
	       }
		
		@Override
		public void serialEvent(SerialPortEvent event) {
			// Serial Port에서 event가 발생하면 호출!
			if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
				// Port를 통해서 데이터가 들어왔다는 의미
				byte[] readBuffer = new byte[128];

				try {

					// Stream 안의 데이터를 반복해서 읽어온다.
					while(bis.available() > 0) {
						bis.read(readBuffer);						
					}

					String result = new String(readBuffer);
					printMsg("받은 메시지는 ___ " + result + "___");
					String[] code = result.split(":");
				
						//for(int i = 1; i <= code.length-1; i++) { *******************************
						if(code[1].contains("U2800000001737461727452333787")) {
							printMsg("RFID들어옴");
							sendData("start");
							System.out.println(getTime());
							
							//String str11 = "1";
							
						}
						else if(code.length > 2){
							for(int i=1;i<4;i++) {
							printMsg("코드길이 : " + code.length);
							//CarLattePanda에서 rfid인식되었다는 신호보낼때(rfid)
							if(code[i].contains("U2800000005")) {
								//printMsg("온도 : " + hexstringtoInt(code[i]));*************************
								printMsg("온도 : " + hexstringtoInt(code[i]) + "i : " + i);							
								//dsoc.send(dp);
							} else if(code[i].contains("U2800000006")) {
								//printMsg("습도 : " + hexstringtoInt(code[i]));***************************
								printMsg("습도 : " + hexstringtoInt(code[i]) + "i : " + i);							
								//dsoc.send(dp);
							}else if(code[i].contains("U2800000007")) {
								//printMsg("습도 : " + hexstringtoInt(code[i]));***************************
								printMsg("정류장번호 : " + hexstringtoInt(code[i]) + "i : " + i);				
								//dsoc.send(dp);
							}
							
							else {
								//printMsg("알수없는 protocol입니다.");
							}
							}
						}
					

						
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println(e);
				}

			}

		}

	}

	public String hexstringtoInt(String str) {
		String number = null;
		if(str.length()>27) {
			String natureHexString = str.substring(11, 23);
			String primeHexString = str.substring(23,27);
			int n = Integer.parseUnsignedInt(natureHexString, 16);
			int p = Integer.parseUnsignedInt(primeHexString, 16);
			number = n + "." + p;
		}
		return number;
		
	}
	
	public static String stringToHex(String s) {
		String result = "";

		for (int i = 0; i < s.length(); i++) {
			result += String.format("%02X", (int) s.charAt(i));
		}

		return result;
	}

	private void printMsg(String msg) {
		Platform.runLater(()->{
			textarea.appendText(msg + "\n");
		});
	}

	private void sendData(String sendMsg) {

		// 데이터 보내기
		try {
			out = serialPort.getOutputStream();
			// CAN 데이터 수신 허용 설정
			//					String ckSum = getCheckSum();
//			String cksum = getCheckSum();
			
			String msg = getCheckSum(sendMsg);
			//					String msg = ":"+cksum+"\r";
			System.out.println(msg);


			// 문자열을 outputStream으로 바로 쏠수 없어서 try-catch
			try {
				byte[] inputData = msg.getBytes();
				out.write(inputData);
				printMsg(portName + "가 송신을 시작합니다.");
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println(e);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private String getCheckSum(String sendMsg) {
		//String data = stringToHex(tf.getText());
		String data = stringToHex(sendMsg);
		String id = "00000002";
		String msg = "W28" + id + data;
		System.out.println(msg);


		int a = 0;
		for (char item : msg.toCharArray()) {
			a += item;
		}
		int result = a & 0xff;
		String b = Integer.toHexString(result).toUpperCase();
		System.out.println(result);
		System.out.println(Integer.toHexString(result).toUpperCase());

		return ":"+msg+b+"\r";
	}

	private void connectPort(String portName) {
		// portName을 이용해 Port에 접근해서 객체를 생성해요.
		try {
			// portName을 이용해서 CommPortIdentifier 객체를 만든다.
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

			printMsg(portName + "에 연결을 시도합니다.");

			// portIdentifier를 이용해서 현재 사용되고 있는지 확인한다.
			if (portIdentifier.isCurrentlyOwned()) {
				printMsg(portName + "가 다른 프로그램에 의해서 사용되고 있어요 ㅠㅠ");				
			}else {
				// Port를 열고 Port 객체를 획득
				commPort = portIdentifier.open("MyApp", 5000);

				// Port 객체(commPort)를 얻은 후 Serial인지 Parallel인지를 확인한 후 적절하게 Type Casting
				if (commPort instanceof SerialPort) {
					// SerialPort이면 True
					serialPort = (SerialPort)commPort;

					// SerialPort에 대한 설정
					serialPort.setSerialPortParams(921600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

					// 포트에 이벤트를 감지하는 리스너를 붙여 메시지가 들어왔을 때 알림을 준다.
					serialPort.addEventListener(new MyPortListener());

					// Serial Port의 데이터가 유효할 때 (데이터가 들어옴) 알려주는 기능을 활성화
					serialPort.notifyOnDataAvailable(true);
					printMsg(portName + "에 리스너가 등록되었어요~");

					// 데이터 받기
					bis = new BufferedInputStream(serialPort.getInputStream());
					// 데이터 보내기
					out = serialPort.getOutputStream();

					// CAN 데이터 수신 허용 설정
					// 프로토콜을 이용해서 정해진 형식대로 문자열을 만들어서 out stream을 통해서 출력
					String msg = ":G11A9\r";

					// 문자열을 outputStream으로 바로 쏠수 없어서 try-catch
					try {
						byte[] inputData = msg.getBytes();
						out.write(inputData);
						printMsg(portName + "가 수신을 시작합니다.");
					} catch (Exception e) {
						// TODO: handle exception
						System.out.println(e);
					}

				}
			}

		}catch (Exception e) {
			// TODO: handle exception
			System.out.println(e);
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		BorderPane root = new BorderPane();

		root.setPrefSize(700, 500);

		textarea = new TextArea();
		root.setCenter(textarea);

		connBtn = new Button("COM,TCP 포트 연결!!");
		connBtn.setPrefSize(200, 100);

		// 람다식을 사용해서 이벤트를 핸들링한다.
		connBtn.setOnAction(t->{
			//String portName = "COM12";

			// portName 연결
			connectPort(portName);
			Runnable runnable = () -> {
				try {
					server = new ServerSocket(8090);
					int flag = 0;

					printMsg("Echo Server 가동 111!! ");

					// Socket s = server.accept(); // 접속 대기 부분. 한줄만 있으면 한명의 클라이언트만 받음
					while (true) {
						printMsg("클라이언트 접속 대기");
						Socket s = server.accept(); // 접속 대기 부분. 여러명의 접속을 위해 무한 루프를 이용
						// accept 떄문에 javaFX가 멈춤. 그래서 Thread를 별개로 만들어야함
						System.out.println(s);
						printMsg("클라이언트 접속 성공");

						ERunnable r = new ERunnable(s); // 클라이언트가 접속했으니 Thread만들고 시작해요!
						executorservice.execute(r); // thread 실행
						// Thread t2 = new Thread(r);

					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		});

		sendBtn = new Button("데이터 전송");
		sendBtn.setPrefSize(200, 100);
		sendBtn.setOnAction(t->{
			//String portName = "COM12";
			sendData(portName);
		});

		// 긴 Panel 하나를 생성
		FlowPane flowpane = new FlowPane();
		flowpane.setPrefSize(700, 50);
		flowpane.getChildren().add(connBtn);
		flowpane.getChildren().add(sendBtn);
		root.setBottom(flowpane);

		// 화면에 띄우기
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("ServerLattePanda");
		primaryStage.show();

	}

}
