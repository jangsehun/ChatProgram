package ChatProgram.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Room {
	List<User> room_userList;
	private int room_num;
	private User room_owner;
//	private MainServer server;

	// 생성자(서버 소켓을 가지고있어야 서버와 통신이 가능하다)
	public Room(int room_num, User user) {
		room_userList = new ArrayList<>();
		this.room_num = room_num;
		this.room_owner = user;
//		this.server = server;
	}

	//해당 룸의 번호를 리턴 해준다.
	public int getRoomNum() {
		return room_num;
	}
	
	//해당 방의 방장을 리턴 해준다.
	public User getRoomOwner() {
		return room_owner;
	}
	
	// 해당 방에 유저를 추가해준다.
	public void addUser(User user) throws IOException {
		room_userList.add(user);
		user.setUserRoomNum(room_num);
		setRoomUserList();
		broadCast("RoomToUser&"+user.getUserName()+"&"+"님이 [ " + room_num+" ] 번방에 들어왔습니다.");
		System.out.println("현재 [ " + room_num+" ] 번방 접속자 수 : "+room_userList.size()); //이 부분도 UI로 표시할 수 있게 되면 메소드화 시켜서 broadCast를 통해 보내면됨
	}
	
	// 해당 방에 있는 유저를 지운다.
	public void removeUser(User user) throws IOException {
		user.setUserRoomNum(0);
		room_userList.remove(user);
		setRoomUserList();
		broadCast("RoomToUser&"+user.getUserName()+"&"+"님이 [ " + room_num+ " ] 번방에서 나갔습니다.");
	}

	//해당 방에 있는 유저들의 닉네임을 클라이언트에 표시해주기 위한 메소드
	public void setRoomUserList() throws IOException {
		for (User userList : room_userList) { //해당 방에 속해있는 유저들에게 
			userList.sendMessage("callTocleanUserList&" + " "); //클라이언트의 유저 목록을 지우라고 시킨다.
			for (int x = 0; x < room_userList.size(); x++) { //해당 방 유저의 수만큼 돌면서 
				userList.sendMessage("callToSetUserList&" + room_userList.get(x).getUserName()); //해당 유저의 닉네임을 보내준다.
			}
		}
	}
	
	//해당 방의 총 유저수를 알려준다.
	public int getRoomUserListCount() {
		return room_userList.size();
	}

	// 해당 방에 있는 유저들에게 방송한다.(메세지를 보낸다)
	public void broadCast(String msg) throws IOException {
		StringTokenizer temp = new StringTokenizer(msg, "&");
		String msgTo;
		String user_Name;
		String user_Message;
		
		msgTo = temp.nextToken();
		user_Name = temp.nextToken();
		user_Message = temp.nextToken();
		
		for (User userList : room_userList) {
			userList.sendMessage(msgTo + "&" + user_Name + " : " + user_Message);
		}
	}
}
