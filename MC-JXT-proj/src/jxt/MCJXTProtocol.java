package jxt;

import it.unisa.dia.gas.jpbc.Element;
import java.security.InvalidAlgorithmParameterException;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import jxt.entity.JXTEDB;
import jxt.entity.JXTTSetBlock;
import jxt.entity.JXTTSetTuple;
import jxt.entity.KeySet;
import jxt.entity.KeywordFilesBlock;
import jxt.entity.Token;
import jxt.entity.ytuple;

import java.awt.Cursor;
import java.awt.print.Printable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.invoke.StringConcatFactory;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.management.monitor.StringMonitor;
import javax.swing.plaf.multi.MultiInternalFrameUI;

import org.bouncycastle.asn1.cmp.InfoTypeAndValue;
import org.bouncycastle.asn1.crmf.EncKeyWithID;
import org.bouncycastle.cert.crmf.AuthenticatorControl;
import org.bouncycastle.crypto.tls.ClientCertificateType;
import org.bouncycastle.jce.provider.JDKKeyFactory.RSA;
import org.hamcrest.core.StringContains;

import sks.entity.KeywordFiles;
import util.AES;
import util.AuthEnc;
import util.DataRec;
import util.Hash;
import util.IntAndByte;
import util.InvertIndex;
import util.TableFilesBlock;
import util.PRP;


import org.sqlite.*;

class XJoinToken {
	Element[] xjointoken1;
	Element[] xjointoken2;
}

public class MCJXTProtocol {
	
	// 密码参数
	public Pairing pairing = PairingFactory.getPairing("params/curves/a.properties");
	public int p = 97;
	public byte[] authKey = IntAndByte.toByteArray(9);
	public byte[] macKey = IntAndByte.toByteArray(10);
	
	
	public int m, n; // 两张表的属性个数
	Vector<String> attrsAll = new Vector<String>(); // 总共的属性列表
	Vector<Integer> T = new Vector<Integer>(); // 用来做join的属性列表

	
	Vector<TableFilesBlock> tabs = new Vector<TableFilesBlock>(); // 明文表
	public Vector<InvertIndex> invIndexs = new Vector<InvertIndex>(); // 倒序索引
	public JXTEDB jxtedb; // EDB(encrypted database)密态数据库
	public Vector<String> policy = new Vector<String>();
	public AuthEnc authEncer = new AuthEnc();

	public MCJXTProtocol(String dbPath, Vector<Integer> Tt) {
		try {
			// 初始化: 从disk load明文表
			genTables(dbPath);
		} catch (Exception e) {
			System.out.print("class not found");
			e.printStackTrace();
		}
		this.m = tabs.get(0).recs.size();
		this.n = tabs.get(0).attrs.size();
		this.T = Tt;
	}
	
	// 从disk数据库中读取明文表
	public void genTables(String dbPath) throws Exception{
		
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
		Statement stmt = conn.createStatement();
		Vector<String> attrs = new Vector<String>();
		attrs.addAll(new ArrayList<String>(Arrays.asList("PERSONID", "FIRSTNAME", "LASTNAME", "SEX",
				"AGE", "STATE", "RACE", "EDUCATION")));
		this.attrsAll = attrs;
		
		String sqlState = "select * from state_census";
		ResultSet res = stmt.executeQuery(sqlState);
		
		Vector<Integer> attrState = new Vector<Integer>();
		attrState.addAll(new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,5)));
		TableFilesBlock stateCensus = new TableFilesBlock(attrState);
		
		while (res.next()) {
			String id = String.valueOf(res.getInt(1));
			String fn = res.getString(2);
			String ln = res.getString(3);
			String sex = res.getString(4);
			String age = String.valueOf(res.getInt(5));
			String state = res.getString(6);
			stateCensus.AddRecord(new ArrayList<String>(Arrays.asList(id, fn, ln, sex, age, state)));
		}
		
		String sqlRace = "select * from race_census";
		res = stmt.executeQuery(sqlRace);
		
		Vector<Integer> attrRace = new Vector<Integer>();
		attrRace.addAll(new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,6)));
		TableFilesBlock raceCensus = new TableFilesBlock(attrRace);
		
		while (res.next()) {
			String id = String.valueOf(res.getInt(1));
			String fn = res.getString(2);
			String ln = res.getString(3);
			String sex = res.getString(4);
			String age = String.valueOf(res.getInt(5));
			String race = res.getString(6);
			raceCensus.AddRecord(new ArrayList<String>(Arrays.asList(id, fn, ln, sex, age, race)));
		}
		// raceCensus.PrintTable();
		this.tabs.add(stateCensus);
		this.tabs.add(raceCensus);
	}
	
	/* Data Provider: SetPolicy
	 * Data Provider设置自己的查询政策，即允许哪些关键字的查询，屏蔽那些关键字的查询
	 */
	public void providerSetPolicy() {
		// Todo
	}
	
	/* Data Provider: EDBSetup
	 * Data Provider将明文数据库作为输入，并生成加密数据库EDB，该数据库将外包给(不受信任的)服务器。 加密数据库EDB由两种数据结构组成——TSet和XSet。
	 * EDBSetup协议还生成一个密钥K，该密钥存储在客户端本地，随后用于生成查询令牌。
	 */
	public void providerEDBSetup() {
		jxtedb = new JXTEDB(tabs.size(), m, n, T.size());
		// 指定key
		byte[] K_I = { 3 };
		byte[] K_W = { 4 };
		byte[] K_Z1 = { 5 };
		byte[] K_Z2 = { 6 };
		byte[] K_enc = { 7 };
		byte[] K_T = { 8 };
		byte[] K_C1 = { 11 };
		byte[] K_C2 = { 12 };
		Vector<Integer> keys = new Vector<Integer>();
		keys.addAll(new ArrayList<Integer>(Arrays.asList(3,4,5,6,7,8,11,12)));
		jxtedb.K.initKey(keys);
		
		long s,e1,e2,e3,e4;
		
		// 生成XSet
		for (int i = 0; i < tabs.size(); i++) {
			TableFilesBlock tabTmp = tabs.get(i);
			for (int j = 0; j < m; j++) {
				DataRec recTmp = tabTmp.recs.get(j);
				byte[] ind = IntAndByte.toByteArray(recTmp.id);

				for (int t = 0; t<T.size(); t++) {
					String wt;
					wt = recTmp.GetValue(T.get(t));
					byte[] xind1 = AES.encrypt(ind, jxtedb.K.K_I);
					byte[] xindt = AES.encrypt(xind1, IntAndByte.toByteArray(t));
					byte[] xwt = AES.encrypt(wt.getBytes(), jxtedb.K.K_W);
					Element xtagt = Hash.HashToZr(pairing, xindt).add(Hash.HashToZr(pairing, xwt)).getImmutable();
					jxtedb.XSet[i].xtagLst.put(xtagt.toString());
				}
			}
		}
		
		// 生成TSet
		for (int i = 0; i < tabs.size(); i++) {
			invIndexs.add(new InvertIndex(tabs.get(i)));
		}

		for (int i = 0; i < tabs.size(); i++) {
			HashMap<String, Vector<Integer>> index = invIndexs.get(i).index;

			for (String w : index.keySet()) {
				Vector<JXTTSetTuple> tuple = new Vector<JXTTSetTuple>();
				byte[] z1 = AES.encrypt(w.concat(String.valueOf(0)).getBytes(), jxtedb.K.K_Z1);	// z_0
				byte[] z2 = AES.encrypt(w.concat(String.valueOf(0)).getBytes(), jxtedb.K.K_Z2); // z_0'
				
				int cnt = 1;
				Vector<Integer> indPRP = PRP.genPRP(index.get(w));
				for (int ind : indPRP) {
					byte[] zcnt1 = AES.encrypt(w.concat(String.valueOf(cnt)).getBytes(), jxtedb.K.K_C1); // z_cnt
					byte[] zcnt2 = AES.encrypt(w.concat(String.valueOf(cnt)).getBytes(), jxtedb.K.K_C2); // z_cnt'
					byte[] kew = AES.encrypt(w.getBytes(), jxtedb.K.K_enc);
					byte[] ct = AES.encrypt(IntAndByte.toByteArray(ind), kew);

					Vector<ytuple> y = new Vector<ytuple>();
					for (int t=0; t<T.size(); t++) {

						String wt = tabs.get(i).recs.get(ind - 1).GetValue(T.get(t));
						byte[] xind1 = AES.encrypt(IntAndByte.toByteArray(ind), jxtedb.K.K_I);
						byte[] xindt = AES.encrypt(xind1, IntAndByte.toByteArray(t));
						byte[] xwt = AES.encrypt(wt.getBytes(), jxtedb.K.K_W);
						Element yt1 = Hash.HashToZr(pairing, xindt).sub(Hash.HashToZr(pairing, z1))
								.sub(Hash.HashToZr(pairing, zcnt1)).getImmutable();
						Element yt2 = Hash.HashToZr(pairing, xwt).sub(Hash.HashToZr(pairing, z2))
								.sub(Hash.HashToZr(pairing, zcnt2)).getImmutable();
						y.add(new ytuple(yt1, yt2, T.get(t)));
					}
					tuple.add(new JXTTSetTuple(ct, y));
					cnt++;
				}
				byte[] stag1 = AES.encrypt(w.getBytes(), jxtedb.K.K_T);
				byte[] ii = IntAndByte.toByteArray(i);
				byte[] stag = AES.encrypt(stag1, ii);
				
				jxtedb.TSet[i].arr.put(new String(stag), tuple);
			}
		}
	}
	
	/* Data Provider: GenTokens
	 * Data Retriever将自己的查询发送给Data Provider，后者首先对其发起的查询进行审查，
	 * 审查通过后生成查询令牌，并给Retriever分发查询令牌
	 */
	public Token providerGenTokens(int i, int j, String w1, String w2, Vector<Integer> joinAttrs) {
		
		// 审查query
		String q = String.valueOf(i)+String.valueOf(j)+w1+w2;
		for (Integer a : joinAttrs)
			q = q + a;
		if (policy.contains(q) == true) {
			System.out.print("Policy auth pass!\n");
		}
		
		// Gen r1, r2 in [1, p], stag1, stag2
		byte[] stag1t = AES.encrypt(w1.getBytes(), jxtedb.K.K_T);
		byte[] ii = IntAndByte.toByteArray(i);
		byte[] stag1 = AES.encrypt(stag1t, ii);

		byte[] stag2t = AES.encrypt(w2.getBytes(), jxtedb.K.K_T);
		byte[] jj = IntAndByte.toByteArray(j);
		byte[] stag2 = AES.encrypt(stag2t, jj);
		
		int r1 = (int)(Math.random()*(p-1))+1;
		int r2 = (int)(Math.random()*(p-1))+1;
		
		Token t = new Token();
		// Gen bxtrap
		byte[] z11 = AES.encrypt(w1.concat(String.valueOf(0)).getBytes(), jxtedb.K.K_Z2);
		t.bxtrap1 = Hash.HashToZr(pairing, z11).
				add(Hash.HashToZr(pairing, IntAndByte.toByteArray(r1))).getImmutable();
		
		byte[] z12 = AES.encrypt(w2.concat(String.valueOf(0)).getBytes(), jxtedb.K.K_Z1);
		t.bxtrap2 = Hash.HashToZr(pairing, z12).
				add(Hash.HashToZr(pairing, IntAndByte.toByteArray(r2))).getImmutable();
		
		// Gen env
		Vector<byte[]> env = new Vector<byte[]>();
		// enc r1,r2
		byte[] er1 = authEncer.EncAndMac(authKey, macKey, IntAndByte.toByteArray(r1));
		byte[] er2 = authEncer.EncAndMac(authKey, macKey, IntAndByte.toByteArray(r2));
		
		// enc stag1, stag2
		byte[] estag1 = authEncer.EncAndMac(authKey, macKey, stag1);
		byte[] estag2 = authEncer.EncAndMac(authKey, macKey, stag2);
		
		env.add(er1);
		env.add(er2);
		env.add(estag1);
		env.add(estag2);
		
		t.env = env;
		return t;
	}
	
	/* Data Retriever: GenXJoinTokens
	 * Data Retriever在向Data Provider请求完查询令牌之后，自己再本地生成最后的查询令牌XJToken
	 */
	public Vector<Element[]> retrieverGenXJoinTokens(String w1, String w2, Element bxtrap1, 
										Element bxtrap2, int Mask) {

		Element[] xjointoken1 = new Element[Mask];
		Element[] xjointoken2 = new Element[Mask];

		for (int i = 1; i <= Mask; i++) {
			
			byte[] zcnt1 = AES.encrypt(w1.concat(String.valueOf(i)).getBytes(), jxtedb.K.K_C2);
			xjointoken1[i - 1] = bxtrap2.add(Hash.HashToZr(pairing, zcnt1)).getImmutable();

			byte[] zcnt2 = AES.encrypt(w2.concat(String.valueOf(i)).getBytes(), jxtedb.K.K_C1);
			xjointoken2[i - 1] = bxtrap1.add(Hash.HashToZr(pairing, zcnt2)).getImmutable();

		}
		Vector<Element[]> x = new Vector<Element[]>();
		x.add(xjointoken1);
		x.add(xjointoken2);

		return x;
	}
	
	/* Data Retriever: Decryption
	 * Data Retriever在接收到来自Cloud Sever的密态查询结果之后，进行解密并得到最后的查询结果
	 */
	public Vector<Vector<Integer>> retrieverDecrypt(Vector<Vector<byte[]>> encResIndPair, byte[] Kw1, byte[] Kw2) {
		Vector<Vector<Integer>> resIndPair = new Vector<Vector<Integer>>();
		for (Vector<byte[]> p : encResIndPair) { // pair (ct1, ct2)
			Vector<Integer> t = new Vector<Integer>();
			Integer ind1 = IntAndByte.toInt(AES.decrypt(p.get(0), Kw1));
			Integer ind2 = IntAndByte.toInt(AES.decrypt(p.get(1), Kw2));
			t.add(ind1);
			t.add(ind2);
			resIndPair.add(t);
		}
		return resIndPair;
	}
	
	
	
	/* Cloud Server: Search
	 * Cloud Server在接收到来自Data Retriever的查询令牌XJoinToken之后在密态数据库上进行搜索，并返回给Retriever密态查询结果
	 */
	public Vector<Vector<byte[]>> serverSearch(Element[] xjointoken1, Element[] xjointoken2, int i, int j, 
			Vector<byte[]> env, Vector<Integer> joinAttrs) {
		
		long s, e1, e2, e3, e4, e5, e6;
		e5 = 0;e6 = 0;
		
		s = System.nanoTime();
		// 验证envelop
		byte[] r1 = authEncer.DecAndVerify(env.get(0), authKey, macKey);
		byte[] r2 = authEncer.DecAndVerify(env.get(1), authKey, macKey);
		byte[] stag1 = authEncer.DecAndVerify(env.get(2), authKey, macKey);
		byte[] stag2 = authEncer.DecAndVerify(env.get(3), authKey, macKey);
		
		e1 = System.nanoTime();
	
		// find t1, t2
		Vector<JXTTSetTuple> t1 = jxtedb.TSet[i].arr.get(new String(stag1));
		Vector<JXTTSetTuple> t2 = jxtedb.TSet[j].arr.get(new String(stag2));
	
		// gen xtoken1
		Element[][] xtoken1 = new Element[t1.size()][joinAttrs.size()];
		for (int c1 = 1; c1 <= t1.size(); c1++) {
			JXTTSetTuple tmp = t1.get(c1-1);
			int cur = 0;
			for (ytuple y : tmp.y) {
				if (joinAttrs.contains(y.t)) {
					xtoken1[c1-1][cur++] = xjointoken1[c1-1].add(y.y2)
								.sub(Hash.HashToZr(pairing, r2)).getImmutable();
				}
			}
		}

		// gen xtoken2
		Element[][] xtoken2 = new Element[t2.size()][joinAttrs.size()];
		for (int c2 = 1; c2 <= t2.size(); c2++) {
			JXTTSetTuple tmp = t2.get(c2-1);
			int cur = 0;
			for (ytuple y : tmp.y) {
				if (joinAttrs.contains(y.t)) {
					xtoken2[c2-1][cur++] = xjointoken2[c2-1].add(y.y1)
								.sub(Hash.HashToZr(pairing, r1)).getImmutable();
				}
			}
		}
		
		Vector<Vector<byte[]>> resIndPair = new Vector<Vector<byte[]>>();
		for (int c1 = 1; c1 <= t1.size(); c1++) {
			for (int c2 = 1; c2 <= t2.size(); c2++) {
				
				if (c1==1 && c2==1)
					e5 = System.nanoTime();
				Boolean flag = true;
				for (int t = 0; t < joinAttrs.size(); t++) {
					
					Element tmp = xtoken1[c1-1][t].add(xtoken2[c2-1][t]).getImmutable();
					// System.out.print(tmp.toString()+"\n");
					if (!jxtedb.XSet[j].xtagLst.mightContain(tmp.toString())) {
					//if (!jxtedb.XSet[j].xtag.contains(tmp.toString())) {
						flag = false;
						break;
					}
				}
				
				// match ,return ct1, ct2
				if (flag == true) {
					Vector<byte[]> ct = new Vector<byte[]>();
					ct.add(t1.get(c1-1).ct);
					ct.add(t2.get(c2-1).ct);
					resIndPair.add(ct);
				}
				if (c1==1 && c2==1)
					e6 = System.nanoTime();
			}
		}
		e3 = System.nanoTime();
		
		return resIndPair;
	}
	
	public static void main(String[] args) {
		
		String rootDir = "./";
		int i = 0;
		int j = 1;
		String w1Raw = "(STATE,California)";
		String w2Raw = "(RACE,White)";
		Vector<Integer> Tt = new Vector<Integer>();
		Tt.addAll(Arrays.asList(0,1,2));
		Vector<Integer> dataSize = new Vector<Integer>();
		for (int c=500; c<=500; c+=500)
			dataSize.add(c);
		
		String dir1 = rootDir + "res.txt";
		
		try {
			File file1 = new File(dir1);
			if (!file1.exists())
				file1.createNewFile();
			
			FileWriter writer1 = new FileWriter(new File(dir1));
			
			for (Integer size : dataSize) {
				
				String dbPath = rootDir+"DB/sample"+String.valueOf(size)+".db";
				MCJXTProtocol jxtp = new MCJXTProtocol(dbPath, Tt);
				
				String a1 =  w1Raw.substring(1,  w1Raw.length()-1);
				String b1 = a1.substring(0, a1.indexOf(","));
				String w1 =  w1Raw.replace(b1, String.valueOf(jxtp.attrsAll.indexOf(b1)));
				
				String a2 = w2Raw.substring(1,w2Raw.length()-1);
				String b2 = a2.substring(0, a2.indexOf(","));
				String w2 = w2Raw.replace(b2, String.valueOf(jxtp.attrsAll.indexOf(b2)));
				
				System.out.print("T nums: "+String.valueOf(jxtp.T.size())+"\n");
				
				// Data owner: EDBSetup
				long timeEDB1 = System.nanoTime();
				jxtp.providerEDBSetup();
				long timeEDB2 = System.nanoTime();
				writer1.write(String.valueOf(timeEDB2-timeEDB1)+" ");

				System.out.printf("EDBSetup time: %d ns\n",timeEDB2-timeEDB1);
				
				for (int c=1; c<=1; c++) {
					
					FileWriter writer = null;
					Vector<String> strjoinAttrs = new Vector<String>();
					strjoinAttrs.addAll(Arrays.asList("PERSONID"));
					writer = writer1;
					
					// Pre-process: 将joinAttr转化为标号
					Vector<Integer> joinAttrs = new Vector<Integer>();
					for (String s : strjoinAttrs) {
						joinAttrs.add(jxtp.attrsAll.indexOf(s));
					}
					
					System.out.print("joinAttr nums: "+String.valueOf(joinAttrs.size())+"\n");
					// Data owner: Set policy
					jxtp.providerSetPolicy();
					
					// Data owner: 审查query, 生成token=(bxtrap, env, KC, KC', K_enc)
					Token token = jxtp.providerGenTokens(i, j, w1, w2, joinAttrs);
					
					// Client: 利用token中获得的k_enc本地计算解密文档id的key
					byte[] Kw1 = AES.encrypt(w1.getBytes(), jxtp.jxtedb.K.K_enc);
					byte[] Kw2 = AES.encrypt(w2.getBytes(), jxtp.jxtedb.K.K_enc);
					
					// Client: 利用token中的bxtrap以及KC1, KC2生成bxjointoken
					Vector<Element[]> bxjointoken = jxtp.retrieverGenXJoinTokens(w1, w2, token.bxtrap1, token.bxtrap2, jxtp.m);
					
					// Sever: 和data onwer share macKey和authKey, 并使用bxjointoken进行Search
					long timeSearch1 = System.nanoTime();
					Vector<Vector<byte[]>> encResIndPair = jxtp.serverSearch(bxjointoken.get(0), bxjointoken.get(1)
							, i, j, token.env, joinAttrs);
					long timeSearch2 = System.nanoTime();
					
					writer.write(String.valueOf(timeSearch2-timeSearch1)+"\n");
					System.out.printf("Search time: %d ns\n",timeSearch2-timeSearch1);
					
					// Client: decrypt and get result
					Vector<Vector<Integer>> resIndPair = jxtp.retrieverDecrypt(encResIndPair, Kw1, Kw2);	
					System.out.print("Search result: "+String.valueOf(resIndPair.size())+" pair\n");
					
				}
			}
			writer1.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
