package ChatProgram.client;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.List;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JPanel;

import ChatProgram.server.User;

public class Client extends Frame implements Runnable{
	Socket clientSocket = null;
	PrintWriter out = null;
	BufferedReader in = null;
	Scanner scn = new Scanner(System.in);
	BufferedReader keyboardInput;
	
	//GUI
	TextArea outputArea;
	TextField inputField;
	List list; //클라이언트(유저)가 속한 방에 유저LIST
	
	//UI 생성
	public Client(String title) {
		super(title);
		setLayout(new BorderLayout());
		
		outputArea = new TextArea();
		outputArea.setEditable(false);
		list = new List();
		
		//해당 채딩방의 모든 대화 출력
		add(outputArea,"Center");
		
		//클라이언트의 채팅 입력란
		inputField = new TextField();
		add(inputField,"South");
		inputField.addActionListener(new InputListener());
		
		//속해있는 방에 유저리스트 출력
		add(list,"West");
	}
	
	//받아온 메세지를 출력
	public void addMessage(String msg){
		outputArea.append(msg);
	}
	
	//해당 방에 유저들을 출력
	public void setUserList(String user) {
		list.add(user);
	}
	
	//해당 방의 유저들을 지워준다.
	public void cleanUserList() {
		list.removeAll();
	}
	
	//OutputArea를 초기화시켜준다.
	public void cleanOutputArea() {
		outputArea.setText("");
	}
	
	//서버와 접속
	public void connect(String host, int port){
		try {
			clientSocket = new Socket(host,port);	//서버 접속
			out = new PrintWriter(clientSocket.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			//처음 접속 할 때 이름을 넘겨준다. UI만들면 UI를 통해서 받아와서 처리하게끔 바꾸자
			out.println("jangsehun" + "&" + "접속완료");
			
		} catch (Exception e) {
			System.err.println("connect 에러입니다.");
			System.exit(1);	//에러일 때 종료 -> 그냥 종료 : System.exit(0); 
		}
	}
	
	public void disconnect() throws IOException {
		clientSocket.close();
		in.close();
		out.close();
	}
	
	public static void main(String[] args) {
		Client client = new Client("클라이언트");
		
		//접속할 서버의 IP, PORT 
		client.connect("127.0.0.1", 9999); //내부적으로 코딩이 완료 되면 입력받아 들어가는걸로 바꿔준다.
		
		
		client.keyboardInput = new BufferedReader(new InputStreamReader(System.in));
		//클라이언트 쓰레드 실행 (run()은 받아온 메세지만 계속뿌려준다.)
		Thread thread = new Thread(client);
		thread.start();
		
		client.pack();
		client.setSize(500, 300);
		client.setVisible(true);
		
		//X 활성화 
		client.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				try {
					client.disconnect();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.exit(0);
			}
		});
		
		//계속 메세지를 입력할수 있는 상태로 만든다.
		while(true) {
			client.outMsg();
			System.out.println();
		}
	}
	
	//서버로 메세지를 보내는 메소드
	public void outMsg() {
		try {
			out.println(keyboardInput.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//계속 실행되며 메세지를 받아온다.
	@Override
	public void run() {
		String str = null;
			try {
				while(true){
					str = in.readLine(); //메세지가 들어오면 
					StringTokenizer temp = new StringTokenizer(str, "&"); //&를 기준으로 토막
					String callToMethod = temp.nextToken();
					String message = temp.nextToken();
					
					//받아온 메세지의 첫번째 토막에 따라 클라이언트의 메소드들을 호출
					switch (callToMethod) {
					case "callToSetUserList":
						setUserList(message);
						break;
					case "callTocleanUserList":
						cleanUserList();
						break;
					case "cleanOutputArea":
						cleanOutputArea();
						break;
					default:
					addMessage(message+"\n");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	class InputListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			String input = inputField.getText();
			inputField.setText("");
			try{
				out.println(input);
			}catch(Exception e1){
				e1.printStackTrace();
			}
		}
	}
}
