package jxt1;

import it.unisa.dia.gas.jpbc.Element;
import java.security.InvalidAlgorithmParameterException;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;


import java.awt.Cursor;
import java.awt.print.Printable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
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

import jxt1.entity.JXTEDB;
import jxt1.entity.JXTTArrBlock;
import jxt1.entity.JXTTSetTuple;
import jxt1.entity.KeySet;
import jxt1.entity.KeywordFilesBlock;
import jxt1.entity.Token;
import jxt1.entity.ytuple;
import sks.entity.KeywordFiles;
import util.AES;
import util.AuthEnc;
import util.DataRec;
import util.Hash;
import util.IntAndByte;
import util.InvertIndex;
import util.TableFilesBlock;
import util.TableManager;

import org.sqlite.*;

class XJoinToken {
	Element[] xjointoken1;
	Element[] xjointoken2;
}

public class MCJXTProtocol {

	// 对称质数阶双线性群
	public Pairing pairing = PairingFactory.getPairing("params/curves/a.properties");
	Vector<TableFilesBlock> tabs = new Vector<TableFilesBlock>();
	public Vector<InvertIndex> invIndexs = new Vector<InvertIndex>();
	public int m, n;
	Vector<String> attrsAll = new Vector<String>();
	Vector<Integer> T = new Vector<Integer>();
	public JXTEDB jxtedb;
	public int p = 97;
	public byte[] authKey = IntAndByte.toByteArray(9);
	public byte[] macKey = IntAndByte.toByteArray(10);
	public AuthEnc authEncer = new AuthEnc();
	public Vector<String> policy = new Vector<String>();

	public MCJXTProtocol() {
		
	}
	
	public void providerSetPolicy() {
		
	}
	
	public Vector<Integer> genPRP(Vector<Integer> ind) {
		Vector<Integer> rind = new Vector<Integer>();
		Vector<Integer> rv = new Vector<Integer>();
		int cursor = 0;
		while (cursor < ind.size()) {
			int r = (int) (Math.random() * 10000 % ind.size());
			while (rv.contains(r) == true) {
				r = (int) (Math.random() * 10000 % ind.size());
			}
			rv.add(r);
			rind.add(ind.get(r));
			cursor++;
		}
		return rind;
	}
	// to be continue
	public void buildInvIndex() {

		for (int i = 0; i < tabs.size(); i++) {
			invIndexs.add(new InvertIndex(tabs.get(i)));
		}
	}
	
	// 从sqlite数据库中读取并构造出表
	public void genTables(String dbPath) throws Exception{
		
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
		Statement stmt = conn.createStatement();
		Vector<String> attrs = new Vector<String>();
		attrs.addAll(new ArrayList<String>(Arrays.asList("PERSONID", "FIRSTNAME", "LASTNAME", "SEX",
				"AGE", "STATE", "RACE", "EDUCATION")));
		this.attrsAll = attrs;
		
		// Construct state_census
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
		
		//stateCensus.PrintTable();
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
		
		//this.T.addAll(Arrays.asList(0,1,2));
		this.T.addAll(Arrays.asList(0));

		this.m = tabs.get(0).recs.size();
		this.n = tabs.get(0).attrs.size();
		
	}
		
	public void providerEDBSetup() {
		// ========假定join的表长度相同,padding的事情回头再考虑========

		/*
		 * 将明文数据库作为输入，并生成加密数据库EDB，该数据库将外包给(不受信任的)服务器。 加密数据库EDB由两种数据结构组成——TSet和XSet。
		 * EDBSetup协议还生成一个密钥K，该密钥存储在客户端本地，随后用于生成查询令牌。
		 */

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
		
		s = System.nanoTime();
		// Gen XSet
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
					jxtedb.XSet[i].xtag.add(xtagt.toString());
				}
			}
		}
		e1 = System.nanoTime();
		
		// Gen TSet
		this.buildInvIndex();
		//System.out.print("========Index1========\n");
		//invIndexs.get(0).PrintIndex();
		//System.out.print("========Index2========\n");
		//invIndexs.get(1).PrintIndex();

		for (int i = 0; i < tabs.size(); i++) {
			HashMap<String, Vector<Integer>> index = invIndexs.get(i).index;

			for (String w : index.keySet()) {
				Vector<JXTTSetTuple> tuple = new Vector<JXTTSetTuple>();
				// z_0
				byte[] z1 = AES.encrypt(w.concat(String.valueOf(0)).getBytes(), jxtedb.K.K_Z1);
				// z_0'
				byte[] z2 = AES.encrypt(w.concat(String.valueOf(0)).getBytes(), jxtedb.K.K_Z2);
				
				int cnt = 1;
				// 此处应当使用random order
				Vector<Integer> indPRP = genPRP(index.get(w));
				for (int ind : indPRP) {
					// z_cnt
					byte[] zcnt1 = AES.encrypt(w.concat(String.valueOf(cnt)).getBytes(), jxtedb.K.K_C1);
					// z_cnt'
					byte[] zcnt2 = AES.encrypt(w.concat(String.valueOf(cnt)).getBytes(), jxtedb.K.K_C2);
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
				// TSet方式来进行
				byte[] stag1 = AES.encrypt(w.getBytes(), jxtedb.K.K_T);
				byte[] ii = IntAndByte.toByteArray(i);
				byte[] stag = AES.encrypt(stag1, ii);
				
				jxtedb.TArr[i].arr.put(new String(stag), tuple);
			}
		}
		e2 = System.nanoTime();
		

		System.out.format("%-20s: %d\n", "XSet setup time", e1-s);
		System.out.format("%-20s: %d\n", "TSet setup time", e2-e1);
	}
	
public Token providerGenTokens(int i, int j, String w1, String w2, Vector<Integer> joinAttrs) {
		
		// 审查query
		String q = String.valueOf(i)+String.valueOf(j)+w1+w2;
		for (Integer a : joinAttrs)
			q = q + a;
		if (policy.contains(q) == true) {
			// to be continue
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

	public Vector<Element[]> clientGenXJoinTokens(String w1, String w2, Element bxtrap1, 
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
		Vector<JXTTSetTuple> t1 = jxtedb.TArr[i].arr.get(new String(stag1));
		Vector<JXTTSetTuple> t2 = jxtedb.TArr[j].arr.get(new String(stag2));
		
		System.out.print("Length of t1:"+String.valueOf(t1.size())+"\n");
		System.out.print("Length of t2:"+String.valueOf(t2.size())+"\n");
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
		e2 = System.nanoTime();
		
		
		Vector<Vector<byte[]>> resIndPair = new Vector<Vector<byte[]>>();
		for (int c1 = 1; c1 <= t1.size(); c1++) {
			for (int c2 = 1; c2 <= t2.size(); c2++) {
				
				if (c1==1 && c2==1)
					e5 = System.nanoTime();
				Boolean flag = true;
				for (int t = 0; t < joinAttrs.size(); t++) {
					
					Element tmp = xtoken1[c1-1][t].add(xtoken2[c2-1][t]).getImmutable();
					// System.out.print(tmp.toString()+"\n");
					if (!jxtedb.XSet[j].xtag.contains(tmp.toString())) {
						flag = false;
						break;
					}
					
				}
				
				// match ,return ct1, ct2
				if (flag == true) {
					//System.out.print("(" + String.valueOf(c1)+","+String.valueOf(c2)+")\n");
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
		
		System.out.format("%-20s: %d\n", "Verify evn time", e1-s);
		System.out.format("%-20s: %d\n", "Gen xtoken time", e2-e1);
		System.out.format("%-20s: %d\n", "Match time", e3-e2);
		System.out.format("%-20s: %d\n", "Contains time", e6-e5);
		
		return resIndPair;
	}
	
	public Vector<Vector<Integer>> benchmark(int i, int j, String w1, String w2, 
										Vector<String> strjoinAttrs, FileWriter writer) throws IOException {
		
		// Pre-process: 将joinAttr转化为标号
		Vector<Integer> joinAttrs = new Vector<Integer>();
		for (String s : strjoinAttrs) {
			joinAttrs.add(attrsAll.indexOf(s));
		}
		// 将w1, w2进行转换
		
		String a1 = w1.substring(1,w1.length()-1);
		String b1 = a1.substring(0, a1.indexOf(","));
		w1 = w1.replace(b1, String.valueOf(attrsAll.indexOf(b1)));
		
		String a2 = w2.substring(1,w2.length()-1);
		String b2 = a2.substring(0, a2.indexOf(","));
		w2 = w2.replace(b2, String.valueOf(attrsAll.indexOf(b2)));
		
		long timeEDB1 = System.nanoTime();
		// Data owner: EDBSetup
		providerEDBSetup();
		long timeEDB2 = System.nanoTime();
		writer.write(String.valueOf(timeEDB2-timeEDB1)+" ");
		System.out.printf("EDBSetup time: %d ns\n",timeEDB2-timeEDB1);
		
		// Data owner: Set policy
		providerSetPolicy();
		
		// Data owner: 审查query, 生成token=(bxtrap, stag, env, KC, KC', K_enc)
		Token token = providerGenTokens(i, j, w1, w2, joinAttrs);
		
		
		// Client: 利用token中获得的k_enc本地计算解密文档id的key
		byte[] Kw1 = AES.encrypt(w1.getBytes(), jxtedb.K.K_enc);
		byte[] Kw2 = AES.encrypt(w2.getBytes(), jxtedb.K.K_enc);
		
		// Client: 利用token中的bxtrap以及KC1, KC2生成bxjointoken
		int Mask = m;
		Vector<Element[]> bxjointoken = this.clientGenXJoinTokens(w1, w2, token.bxtrap1, 
														token.bxtrap2, Mask);
		long timeSearch1 = System.nanoTime();
		// Sever: 和data onwer share macKey和authKey, 并使用bxjointoken进行Search
		Vector<Vector<byte[]>> encResIndPair = serverSearch(bxjointoken.get(0), bxjointoken.get(1)
				, i, j, token.env, joinAttrs);
		long timeSearch2 = System.nanoTime();
		
		writer.write(String.valueOf(timeSearch2-timeSearch1)+"\n");
		System.out.printf("Search time: %d ns\n",timeSearch2-timeSearch1);
		
		
		// Client: decrypt and get result
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
	
	public static void main(String[] args) {
		
		//String w1 = "(STATE,California)";
		//String w2 = "(RACE,White)";
		String w1 = "(STATE,California)";
		String w2 = "(RACE,White)";
		
		Vector<String> joinAttr = new Vector<String>();
		//joinAttr.add("FIRSTNAME");
		//joinAttr.add("LASTNAME");
		joinAttr.add("PERSONID");
		
		String dir = "/Users/lzl/Doc/Notes/research/code/RES/benchmark[100-8000]5.txt";
		File file = new File(dir);
		try {
			if (!file.exists())
				file.createNewFile();

			FileWriter writer = new FileWriter(file);
			
			Vector<Integer> dataSize = new Vector<Integer>();
			dataSize.addAll(new ArrayList<Integer>(Arrays.asList(100, 500, 1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000)));
			//dataSize.addAll(new ArrayList<Integer>(Arrays.asList(2000)));
			for (Integer size : dataSize) {
	
				System.out.print("=======================Benchmark"+String.valueOf(size)+"=======================\n");
				MCJXTProtocol jxtp = new MCJXTProtocol();
				String dbPath = "/Users/lzl/Doc/Notes/research/code/DB/sample"+String.valueOf(size)+".db";
				try {
					long genTablesTime1 = System.nanoTime();
					jxtp.genTables(dbPath);
					long genTablesTime2 = System.nanoTime();
					
					System.out.printf("GenTables time: %d ns\n",genTablesTime2-genTablesTime1);
					writer.write(String.valueOf(genTablesTime2-genTablesTime1)+" ");
					
				} catch (Exception e) {
					System.out.print("class not found");
					e.printStackTrace();
				}
				
				Vector<Vector<Integer>> res = jxtp.benchmark(0, 1, w1, w2, joinAttr, writer);
				System.out.print("Search result: "+String.valueOf(res.size())+" pair\n");
			}
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		/*
		
		*/
		/*
		for (Vector<Integer> v : res) {
			System.out.print("(" + v.get(0) + "," + v.get(1) + ")\n");
			//System.out.print( v.get(0) +" ");
		}
		*/
	}
}
