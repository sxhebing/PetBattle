package controller;

import java.util.Random;
import bean.UserInfo;
import bean.UserLogin;
import game.Player;
import pers.jc.engine.JCManager;
import pers.jc.mvc.Controller;
import pers.jc.sql.CURD;
import pers.jc.sql.SQL;
import pers.jc.sql.Transaction;
import pers.jc.util.JCLogger;
import result.RequestResult;

@Controller
public class UserController {
	
	public static RequestResult login(Player player, String username, String password) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		RequestResult requestResult = new RequestResult();
		UserLogin userLogin = CURD.selectOne(UserLogin.class, new SQL(){{
			WHERE("username=" + PARAM(username));
		}});
		if (userLogin == null) {
			requestResult.setMsg("���˺�δע��");
			return requestResult; 
		}
		if (!userLogin.getPassword().equals(password)) {
			requestResult.setMsg("�˺����벻ƥ��");
			return requestResult; 
		}
		UserInfo userInfo = CURD.selectOne(UserInfo.class, new SQL(){{
			WHERE("id=" + PARAM(userLogin.getId()));
		}});
		if (userInfo == null) {
			requestResult.setMsg("�û���Ϣ��ȡʧ��");
		} else {
			synchronized ("login") {
				Player user = (Player) JCManager.getEntityById(userInfo.getId());
				if (user == null) {
					player.id = userInfo.getId();
					player.userInfo = userInfo;
					JCManager.addEntity(player);
					requestResult.setCode(200);
					requestResult.setData(userInfo);
					requestResult.setMsg("��¼�ɹ�");
					JCLogger.info("(ID:" + userInfo.getId() + ")[" + userInfo.getNickname() + "]��¼��Ϸ");
				} else {
					requestResult.setMsg("���˺�ռ����");
				}
			}
		}
		return requestResult;
	}
	
	public RequestResult register(String username, String password) {
		RequestResult requestResult = new RequestResult();
		UserLogin userLogin = CURD.selectOne(UserLogin.class, new SQL(){{
			WHERE("username=" + PARAM(username));
		}});
		if (userLogin != null) {
			requestResult.setMsg("���˺��ѱ�ע��");
			return requestResult;
		}
		UserInfo user_info = new UserInfo();
		new Transaction() {
			@Override
			public void run() throws Exception {
				UserLogin user_login = new UserLogin();
				user_login.setUsername(username);
				user_login.setPassword(password);
				insertAndGenerateKeys(user_login);
				user_info.setId(user_login.getId());
				user_info.setNickname("���" + user_login.getId());
				if (new Random().nextInt(100) < 50) {
					user_info.setGender(1);
					user_info.setAvatarUrl("Texture/Icon/HeadPhoto/6901");
				} else {
					user_info.setGender(2);
					user_info.setAvatarUrl("Texture/Icon/HeadPhoto/6902");
				}
				insert(user_info);
				commit();
			}
			@Override
			public void success() {
				JCLogger.info("(ID:" + user_info.getId() + ")[" + user_info.getNickname() + "]ע��ɹ�");
				requestResult.setCode(200);
				requestResult.setMsg("ע��ɹ�");
			}
			@Override
			public void fail() {
				requestResult.setMsg("ע��ʧ��");
			}
		};
		return requestResult;
	}
}
