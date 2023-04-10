package util;

import java.nio.charset.StandardCharsets;

import com.google.common.hash.*;


public class BloomFilterTest {
	public static void main(String[] args) {
		 BloomFilter<CharSequence> bloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 
				 												4000, 0.001);
		    int n = 4000;
		    for (int i = 0; i < n; i++) {
		        bloomFilter.put(String.valueOf(i));
		    }
		    int count = 0;
		    for (int i = 0; i < (n * 2); i++) {
		        if (bloomFilter.mightContain(String.valueOf(i))) {
		            count++;
		        }
		    }
		    System.out.println("过滤器误判率：" + 1.0 * (count - n) / n);
	}
}