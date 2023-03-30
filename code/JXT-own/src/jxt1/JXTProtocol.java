package jxt1;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.awt.Cursor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.bouncycastle.asn1.cmp.InfoTypeAndValue;
import org.bouncycastle.crypto.tls.ClientCertificateType;

import jxt1.entity.JXTEDB;
import jxt1.entity.JXTTArrBlock;
import jxt1.entity.JXTTSetTuple;
import jxt1.entity.KeySet;
import jxt1.entity.KeywordFilesBlock;
import jxt1.entity.ytuple;
import sks.entity.KeywordFiles;
import util.AES;
import util.DataRec;
import util.Hash;
import util.IntAndByte;
import util.InvertIndex;
import util.TableFilesBlock;
import util.TableManager;

class XJoinToken {
	Element[] xjointoken1;
	Element[] xjointoken2;
}

public class JXTProtocol {

	// 对称质数阶双线性群
	public Pairing pairing = PairingFactory.getPairing("params/curves/a.properties");
	Vector<TableFilesBlock> tabs = new Vector<TableFilesBlock>();
	public Vector<InvertIndex> invIndexs = new Vector<InvertIndex>();
	public int m, n;
	Vector<String> T = new Vector<String>();
	public JXTEDB jxtedb;

	public JXTProtocol() {
		
	}

	public JXTProtocol(Vector<TableFilesBlock> t, Vector<String> T) {
		this.tabs = t;
		this.m = t.get(0).GetRecsNum();
		this.n = t.get(0).GetAttrsNum();
		this.T = T; // 标号形式
	}

	// to be continue
	public void BuildInvIndex() {

		for (int i = 0; i < tabs.size(); i++) {
			invIndexs.add(new InvertIndex(tabs.get(i)));
		}
	}

	public void GenDemoTab() {
		// 生成table1
		Vector<String> attrsTab1 = new Vector<String>();
		attrsTab1.addAll(new ArrayList<String>(Arrays.asList("Num", "Name", "Age", "Addr")));
		TableFilesBlock tab1 = new TableFilesBlock(attrsTab1);

		tab1.AddRecord(new ArrayList<String>(Arrays.asList("1", "n1", "18", "S")));
		tab1.AddRecord(new ArrayList<String>(Arrays.asList("2", "n2", "20", "B")));
		tab1.AddRecord(new ArrayList<String>(Arrays.asList("3", "n3", "21", "G")));
		tab1.AddRecord(new ArrayList<String>(Arrays.asList("4", "n4", "18", "K")));
		tab1.AddRecord(new ArrayList<String>(Arrays.asList("5", "n1", "27", "Q")));
		tab1.AddRecord(new ArrayList<String>(Arrays.asList("6", "n5", "18", "P")));

		// 生成table2
		Vector<String> attrsTab2 = new Vector<String>();
		attrsTab2.addAll(new ArrayList<String>(Arrays.asList("Num", "Name", "Class", "Grade")));
		TableFilesBlock tab2 = new TableFilesBlock(attrsTab2);

		tab2.AddRecord(new ArrayList<String>(Arrays.asList("1", "n1", "M", "90")));
		tab2.AddRecord(new ArrayList<String>(Arrays.asList("2", "n2", "Y", "100")));
		tab2.AddRecord(new ArrayList<String>(Arrays.asList("4", "n4", "M", "95")));
		tab2.AddRecord(new ArrayList<String>(Arrays.asList("8", "n8", "M", "100")));
		tab2.AddRecord(new ArrayList<String>(Arrays.asList("9", "n9", "M", "97")));
		tab2.AddRecord(new ArrayList<String>(Arrays.asList("6", "n5", "M", "94")));

		this.tabs.add(tab1);
		this.tabs.add(tab2);

		this.T.addAll(Arrays.asList("Num", "Name"));
		this.m = tabs.get(0).recs.size();
		this.n = tabs.get(0).attrs.size();

		// 输出
		// tab1.PrintTable();
		// tab2.PrintTable();
	}
	
	public Vector<Integer> GenPRP(Vector<Integer> ind) {
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
	
	public void EDBSetup() {
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
		jxtedb.K.K_I = K_I;
		jxtedb.K.K_W = K_W;
		jxtedb.K.K_Z1 = K_Z1;
		jxtedb.K.K_Z2 = K_Z2;
		jxtedb.K.K_enc = K_enc;
		jxtedb.K.K_T = K_T;

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

					// HashToZr: map value to Z_p^*(Z_r^*), 为什么加法要这样做呢?????
					Element xtagt = Hash.HashToZr(pairing, xindt).add(Hash.HashToZr(pairing, xwt)).getImmutable();
					jxtedb.XSet[i].xtag.add(String.valueOf(xtagt));
				}
			}
		}

		// Gen TSet
		// Gen Tarr[]
		this.BuildInvIndex();
		System.out.print("========Index1========\n");
		invIndexs.get(0).PrintIndex();
		System.out.print("========Index2========\n");
		invIndexs.get(1).PrintIndex();

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
				
				for (int ind : GenPRP(index.get(w))) {
				
					// z_cnt
					byte[] zcnt1 = AES.encrypt(w.concat(String.valueOf(cnt)).getBytes(), jxtedb.K.K_Z1);
					// z_cnt'
					byte[] zcnt2 = AES.encrypt(w.concat(String.valueOf(cnt)).getBytes(), jxtedb.K.K_Z2);
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
						y.add(new ytuple(yt1, yt2, tabs.get(i).attrs.indexOf(t) + 1));
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
	}

	public Vector<Element[]> GenXjointoken(String w1, String w2, int Mask) {

		Element[] xjointoken1 = new Element[Mask];
		Element[] xjointoken2 = new Element[Mask];

		for (int i = 1; i <= Mask; i++) {

			byte[] z11 = AES.encrypt(w1.concat(String.valueOf(i)).getBytes(), jxtedb.K.K_Z2);
			byte[] z12 = AES.encrypt(w2.concat(String.valueOf(0)).getBytes(), jxtedb.K.K_Z1);
			xjointoken1[i - 1] = Hash.HashToZr(pairing, z11).add(Hash.HashToZr(pairing, z12)).getImmutable();

			byte[] z21 = AES.encrypt(w1.concat(String.valueOf(0)).getBytes(), jxtedb.K.K_Z2);
			byte[] z22 = AES.encrypt(w2.concat(String.valueOf(i)).getBytes(), jxtedb.K.K_Z1);
			xjointoken2[i - 1] = Hash.HashToZr(pairing, z21).add(Hash.HashToZr(pairing, z22)).getImmutable();

		}
		Vector<Element[]> x = new Vector<Element[]>();
		x.add(xjointoken1);
		x.add(xjointoken2);

		return x;
	}

	public Vector<Vector<byte[]>> Search(Element[] xjointoken1, Element[] xjointoken2, int i, int j, byte[] stag1,
			byte[] stag2, Vector<Integer> joinAttrs) {

		Vector<Vector<byte[]>> resIndPair = new Vector<Vector<byte[]>>();

		// find t1, t2
		Vector<JXTTSetTuple> t1 = jxtedb.TArr[i].arr.get(new String(stag1));
		Vector<JXTTSetTuple> t2 = jxtedb.TArr[j].arr.get(new String(stag2));

		// gen xtoken1
		Element[][] xtoken1 = new Element[t1.size()][joinAttrs.size()];
		for (int c1 = 1; c1 <= t1.size(); c1++) {
			JXTTSetTuple tmp = t1.get(c1-1);
			int cur = 0;
			for (ytuple y : tmp.y) {
				if (joinAttrs.contains(y.t)) {
					xtoken1[c1-1][cur++] = xjointoken1[c1-1].add(y.y2).getImmutable();
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
					xtoken2[c2-1][cur++] = xjointoken2[c2-1].add(y.y1).getImmutable();
				}
			}
		}

		for (int c1 = 1; c1 <= t1.size(); c1++) {

			for (int c2 = 1; c2 <= t2.size(); c2++) {

				Boolean flag = true;
				for (int t = 0; t < joinAttrs.size(); t++) {
		
					Element tmp = xtoken1[c1-1][t].add(xtoken2[c2-1][t]).getImmutable();
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
			}
		}
		return resIndPair;
	}

	public Vector<Vector<Integer>> Perform(int i, int j, String w1, String w2, Vector<String> strjoinAttrs) {
		
		// 将joinAttr转化为标号
		Vector<Integer> joinAttrs = new Vector<Integer>();
		for (String s : strjoinAttrs) {
			joinAttrs.add(T.indexOf(s));
		}
		
		// this.jxtedb生成
		EDBSetup();

		// Client: 本地计算Key
		byte[] Kw1 = AES.encrypt(w1.getBytes(), jxtedb.K.K_enc);
		byte[] Kw2 = AES.encrypt(w2.getBytes(), jxtedb.K.K_enc);

		// Client: 生成stag
		byte[] stag1t = AES.encrypt(w1.getBytes(), jxtedb.K.K_T);
		byte[] ii = IntAndByte.toByteArray(i);
		byte[] stag1 = AES.encrypt(stag1t, ii);

		byte[] stag2t = AES.encrypt(w2.getBytes(), jxtedb.K.K_T);
		byte[] jj = IntAndByte.toByteArray(j);
		byte[] stag2 = AES.encrypt(stag2t, jj);

		// Client: 生成xjointoken
		int Mask = m;
		Vector<Element[]> xjointoken = this.GenXjointoken(w1, w2, Mask);

		// Sever: search
		Vector<Vector<byte[]>> encResIndPair = Search(xjointoken.get(0), xjointoken.get(1), i, j, stag1, stag2,
				joinAttrs);

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

		JXTProtocol jxtp = new JXTProtocol();
		jxtp.GenDemoTab();
		String w1 = "(Age,18)";
		String w2 = "(Class,M)";
		Vector<String> joinAttr = new Vector<String>();
		joinAttr.add("Num");
		joinAttr.add("Name");
		
		Vector<Vector<Integer>> res = jxtp.Perform(0, 1, w1, w2, joinAttr);
		
		System.out.print("Search result:\n");
		for (Vector<Integer> v : res) {
			System.out.print("(" + v.get(0) + "," + v.get(1) + ")\n");
		}
		
	}
}
