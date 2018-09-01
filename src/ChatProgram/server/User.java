package ChatProgram.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class User extends Thread {
	// 멤버변수
	private MainServer server;
	private Socket socket;

	private String userName;
	private int userRoomNum;
	private PrintWriter out;
	private BufferedReader in;

	// 생성자
	public User(MainServer server, Socket socket) {
		this.server = server;
		this.socket = socket;
	}

	// userName 의 getter & setter (닉네임을 바꿀경우)
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	// UserRoomNum 의 getter & setter
	public int getUserRoomNum() {
		return userRoomNum;
	}

	public void setUserRoomNum(int userRoomNum) {
		this.userRoomNum = userRoomNum;
	}

	public void sendMessage(String msg) throws IOException {
		if (out != null) {
			out.println(msg);
		}
	}

	@Override
	public void run() {
		try {
			// 메세지 입출력 객체를 소켓에서 받아온다.
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// 접속하자마자 클라이언트에서는 클라이언트 정보를 바로 보내준다.
			// 해당 정보는 "&" 로 구분되어 클라이언트 쓰레드에 저장된다.
			StringTokenizer temp = new StringTokenizer(in.readLine(), "&");
			userName = temp.nextToken();
			userRoomNum = 0; // 아무방에도 들어가지 않았다는걸 뜻한다.(대기실에 접속)
			
			// 서버 콘솔에 뿌려준다.
			System.out.println(getUserName() + " :: 클라이언트 정보 : " + socket + " 접속");
			
			// 만약 User객체의 userRoomNum이 바뀌면
			// 유저의 setUserRoomNum을 통해 바꿔주고, server.sendMessage를 호출한다.
			// 방이 바뀌면 내가 해당 방에 접속했다라는 걸 접속한 방의 모든 유져에게 알려야 하기 때문.
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				switch (inputLine.toString()) {
				case "/back": //해당 방에서 나가기 버튼을 눌렀을때 클라이언트에서 이렇게 보내온다.
					server.userComand("eixtRoom&"+userRoomNum,this);
					break;
				case "/rc1":
					server.userComand("enterRoom&1",this);
					break;
				case "/rc2": //두번째 방으로 옴겨라.
					server.userComand("enterRoom&2",this);
					break;
				case "/cr": //방을 만드는 명령어는 대기실에서만 가능하기때문에 
					server.userComand("createRoom&0", this);
					break;
				default:
					server.sendMessage(userRoomNum + "&" + userName + "&" + inputLine);
				}
			}
			
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {
			server.removeUser(this);
			server.sendMessage(userName + "님이 나가셨습니다.");
			System.out.println("클라이언트 \n" + socket + "\n에서 접속이 끊겼습니다...");
		}
	}
}