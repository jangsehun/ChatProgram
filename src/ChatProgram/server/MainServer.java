package ChatProgram.server;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.TextArea;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

public class MainServer extends Frame {
	final static int PORT = 9999;
	static int m_RoomNum = 1;

	//서버에 접속한 전체 유저 리스트
	Vector<User> userList;
	List<Room> roomList;

	TextArea outputArea;

	// 클라이언트 관리를 벡터로 생성
	public MainServer(String title) {
		userList = new Vector<>();		//전체 유저를 관리
		roomList = new ArrayList<>();	//전체 방을 관리

		/** ui 셋팅*/
		setTitle(title);
		setLayout(new BorderLayout());

		outputArea = new TextArea();
		outputArea.setEditable(false);

		add(outputArea, "Center");
	}

	// 클라이언트(User) 객체 추가_서버에 접속하면
	public void addUser(User user) {
		userList.addElement(user);
	}

	// 클라이언트(User) 객체 삭제_접속이 끈키면
	public void removeUser(User user) {
		userList.removeElement(user);
	}
	
	// Room 객체 생성
	public void addRoom(Room room) {
		roomList.add(room);
	}

	// Room 객체 삭제
	public void removeRoom(Room	room) {
		roomList.remove(room);
		m_RoomNum--;
	}

	// 귓속말 ** 완성안됨
	public void earMessage(String msg) {
		StringTokenizer temp = new StringTokenizer(msg, "&");
		String name;
		String message;

		name = temp.nextToken();
		message = temp.nextToken();
		try {

			for (User list : userList) {
				if (list.getUserName() == name) {
					list.sendMessage(message);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// User에서 메세지를 보내면 해당 메세지에는 항상 User의 방번호,이름이 "&" 을 기준으로 함께 들어온다.
	public void sendMessage(String msg) { 
		// 서버에 온 메세지들 항상 출력
		outputArea.append(msg + "\n");
		// 받아온 메세지 자르고
		StringTokenizer temp = new StringTokenizer(msg, "&");
		
		//토막내어 저장할 변수 선언.
		int room_num;
		String name;
		String message;
		
		// 각각의 변수에 방번호, 이름, 메세지를 나눠서 저장한다.
		room_num = Integer.parseInt(temp.nextToken());
		name = temp.nextToken();
		message = temp.nextToken();
		
		//메세지를 보낸 User의 방번호에 따라 해당 방의 broadCast에 메세지를 넘겨준다.
		try {
			//유저가 보내온 메세지에 포함되어있는 방번호가 0이면 (유저가 아무 방에도 속해있지않다면)
			if(room_num == 0) {
				for(User userList : userList) {
					if(userList.getUserRoomNum()==0) {
						userList.sendMessage("serverAllmsg&"+name+" : "+ message);
					}
				}
			} else {
				//해당 방에 메세지를 전달한다.
				for(Room roomList : roomList) {
					if (room_num == roomList.getRoomNum()) {
						roomList.broadCast("serverToRoom&" + name + "&" + message);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// User가 보낸 명령어에 따라 User의 상태를 바꾼다.(룸을 교체한다든지 등.)
	public synchronized void userComand(String msg, User user) throws IOException {
		StringTokenizer temp = new StringTokenizer(msg, "&");
		String comand;
		int userRoomNum;
		// 각각의 변수에 방번호, 이름을 나눠서 저장한다.
		comand = temp.nextToken();
		userRoomNum = Integer.parseInt(temp.nextToken());
		
		switch(comand) {
		case "enterRoom" : //방을 들어가는 명령어
			for(Room roomList : roomList) {
				if(roomList.getRoomNum() == userRoomNum) {
					roomList.addUser(user);
					user.sendMessage("cleanOutputArea&"+"Client의 대화창을 초기화시켜준다.");
				}
			}
			break;
			
		case "eixtRoom" : //방을 나가는 명령어
			Iterator<Room> iter = roomList.iterator(); //roomList를 iterator 
			while(iter.hasNext()) { //iter에 Room 객체가있으면 
				Room room = iter.next(); //객체를 하나꺼내와서 
				if((room.getRoomNum()) == userRoomNum && room.getRoomUserListCount() == 1) { //확인작업을 한다. 이 명령어를 친 유저의 번호와 방의 번호가 맞는지 && 그 방에 유저가 혼자있는지
					iter.remove(); //해당 방에 유저가 혼자있다면, 즉 eixtRoom 명령어를 입력한 유저가 '방에 혼자있었던 유저' 라면 MainServer에서 해당 방을 지운다.
					m_RoomNum --; //방이 생성될때마다 방에게 할당되는 방 번호(int값)를  -1 해준다. 
					user.setUserRoomNum(0); //해당 유저의 방 정보를 0(아무방에도 속해있지 않는 상태)으로 초기화해준다.
					
					//해당 유저의 클라이언트에게 명령어를 전달한다.
					user.sendMessage("callTocleanUserList&"+"Client의 userList를 초기화 시켜준다."); 
					user.sendMessage("cleanOutputArea&"+"Client의 대화창을 초기화시켜준다.");
					
				}else {
					//위의 조건에 맞지 않다면, 방의 인원이 1명을 초과했다면(2명이상) 
					//해당 방에게 user를 지우라고 한다. 
					room.removeUser(user);
					
					//해당 유저의 클라이언트에게 명령어를 전달한다.
					user.sendMessage("callTocleanUserList&"+"Client의 userList를 초기화 시켜준다.");
					user.sendMessage("cleanOutputArea&"+"Client의 대화창을 초기화시켜준다.");
				}
			}
			break;
			
		case "createRoom" : //방을 만드는 명령어
			Room room = new Room(m_RoomNum,user); //여기가 this에서 user로 바뀜
			roomList.add(room);
			roomList.get(m_RoomNum-1).addUser(user);
			user.sendMessage("cleanOutputArea&"+"Client의 대화창을 초기화시켜준다.");
			m_RoomNum ++;
			break;
		}
	}
	
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		// 클라이언트를 관리하는 객체(추가, 삭제, 메세지전달)
		MainServer mainServer = new MainServer("서버 GUI");

		// GUI
		mainServer.pack();
		mainServer.setSize(500, 300);
		mainServer.setVisible(true);

		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.err.println("연결 실패");
			System.exit(1);
		}
		System.out.println("서버 " + serverSocket + " 에서 연결을 기다립니다.");

		try {
			while (true) {
				Socket serviceSocket = serverSocket.accept();
				// 클라이언트와 통신하는 userThread를 생성하고 실행시킨다.
				User user = new User(mainServer, serviceSocket);
				user.start();
				
				// mainServer의 userList에 User(userThread = 클라이언트)를 추가해준다.
				mainServer.addUser(user);
			}

		} catch (Exception e) {
			try {
				serverSocket.close();// 서버종료
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}
}
