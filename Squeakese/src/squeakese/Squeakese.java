package squeakese;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Squeakese {
	
	/*
	 * Dictionary containing pre-defined (human, non-computer)
	 * translations
	 */
	HashMap<String, String> dictionary, Bdictionary;
	/*
	 * HashMap which maps an english word -> squeakese word (for map)
	 * and does the same backwards for Bmap (squeakese word -> english word)
	 * Future addition: Change mapping from String -> String to
	 * String -> (wordObject), where wordObject is a class which contains
	 * variables for the translated word, the part of speech, the adjective
	 * noun agreement, etc.
	 */
	HashMap<String, String> map, Bmap;
	
	/* 
	 * length of current dictionary
	 */
	int len;
	
	/*
	 * Used as a global variable when
	 * setting up the dictionary
	 */
	int lineNum;
	
	/*
	 * Offset used to configure specific translation for this instance of Squeakese:
	 * allows some basic cryptography and customization
	 */
	int offset;
	
	public Squeakese() throws FileNotFoundException {
		this(0);
	}
	
	public Squeakese(int offset) throws FileNotFoundException {
		setUpTranslator();
		this.offset = offset % 5;
	}

	/*
	 * Enables the translation of an input String
	 */
	public void translate() {
		Scanner scan = new Scanner(System.in);
		boolean isEnglish = true;
		System.out.println("Please input your English sentence.");
		while(scan.hasNextLine()) {
			String s = scan.nextLine();
			if(s.contains("stopTranslate")) {
				System.out.println("Stopping translation.");
				scan.close();
				return;
			}
			if(s.contains("switch") || s.contains("swap")) {
				if(isEnglish) { System.out.println("Switching to Squeakese.");
				} else { System.out.println("Switching to English."); }
				isEnglish = !isEnglish;
				continue;
			}
			if(isEnglish) {
				String[] sArr = s.split(" ");
				ArrayList<String> list = new ArrayList<String>();
					for(String str: sArr) {
						String temp = translateWord(str);
						if(!temp.equals("")) list.add(temp);
					}
				System.out.println(String.join(" ", list));
			} else {
				String[] sArr = s.split("  ");
				ArrayList<String> list = new ArrayList<String>();
					for(String str: sArr) {
						String temp = translateSqueak(str + " ");
						if(!temp.equals("")) list.add(temp);
					}
				System.out.println(String.join(" ", list));
			}
		}
		scan.close();
	}
	
	/*
	 * Translate a word from English into Squeakese
	 * TODO: Add support for plurality
	 */
	public String translateWord(String english) {
		if(!map.containsKey(english.toLowerCase())) return "";
		if(dictionary.containsKey(english)) return dictionary.get(english) + " ";
		if(map.containsKey(english)) return map.get(english);
		return "";
	}
	
	/*
	 * Translates a Squeakese "word" into English
	 */
	public String translateSqueak(String squeak) {
		String tempSqueak = squeak.substring(0, squeak.length()-1);
		if(Bdictionary.containsKey(tempSqueak)) return Bdictionary.get(tempSqueak);
		if(Bmap.containsKey(squeak)) return Bmap.get(squeak);
		return "";
	}
	
	/*
	 * Sets up the Hash Maps needed for translation
	 */
	public void setUpTranslator() throws FileNotFoundException {
		File preDef = new File("predefined.txt");
		Scanner scan = new Scanner(preDef);
		dictionary = new HashMap<String, String>();
		Bdictionary = new HashMap<String, String>();
		len = Integer.parseInt(scan.nextLine());
		while(scan.hasNextLine()) {
			String str = scan.nextLine();
			int pos = str.indexOf(":");
			String t1 = str.substring(0,pos);
			String t2 = str.substring(pos+1, str.length());
			dictionary.put(t1, t2);
			Bdictionary.put(t2, t1);
		}
		scan.close();
		File dict = new File("dictionary.txt");
		scan = new Scanner(dict);
		map = new HashMap<String, String>();
		Bmap = new HashMap<String, String>();
		lineNum = 0;
		while(scan.hasNextLine()) {
			String tStr = getTranslation();
			String str = scan.nextLine();
			str = simplify(str);
			map.put(str, tStr);
			Bmap.put(tStr, str);
			lineNum++;
		}
		scan.close();
	}
	
	/*
	 * Procedurally generated translation for all words not manually predefined.
	 * Importantly the parameters of this can be changed so as to output a
	 * different translation depending on several (only one currently) instantiation
	 * variables.
	 */
	public String getTranslation() {
		lineNum--;
		String temp = "";
		do {
			lineNum++;
			int curLine = lineNum;
			int numWords = 1;
			int index = 0;
			int permutations = 5;
			int ceil = 0;
			for(int i = 0; i < 4; i++) {
				ceil += (int)Math.pow(5, i+1) * (int)Math.pow(2, i);
				if(curLine <= ceil) break;
				permutations = (int)Math.pow(5, i+1) * (int)Math.pow(2, i);
				index = ceil + 1;
				numWords++;
			}
			ceil = ceil - index;
			curLine = curLine - index;
			//abc; a,bc; ab,c; a,b,c;  abcd; a,bcd; ab,cd; abc,d; a,b,cd; ab,c,d; a,bc,d; a,b,c,d;
			// 00, 10, 01, 11           000, 100,   010,    001,   110,    011,    101,    111
			temp = makeString(permutations, ceil, curLine, numWords);
		} while(Bdictionary.containsKey(temp.substring(0, temp.length()-1)));
		return temp;
	}
	
	public String makeString(int perms, int ceiling, int curLine, int numWords) {
		String[] base = {"squeak", "squeaky", "squeaker", "squeakin", "squeakity"};
		StringBuilder sb = new StringBuilder();
		int commas = curLine / perms;
		int[] index = convertBase(numWords, curLine % perms, base);
		for(int i = 0; i < index.length; i++) {
			sb.append(base[index[i]]);
			if(numWords != 0) {
				if((commas & 1) == 1) sb.append(",");
				commas >>= 1;
				numWords--;
			}
			sb.append(" ");
		}
		return new String(sb);
	}
	
	/*
	 * Helper method for makeString. Converts the base of the index in dictionary
	 * to get a new int which represents indexes in the String[] base
	 * e.g. numWords = 3, n = 124, base.length = base = 5
	 * 		new base -> nums -> {4, 4, 4} (this is 124 in base 5)
	 * 		now we can index base[4], base[4], base[4] to get the new String
	 */
	public int[] convertBase(int numWords, int n, String[] base) {
		int[] nums = new int[numWords];
		numWords--;
		int b = base.length;
		while(n > 0) {
			nums[numWords] = ((n % b) + offset) % b;
			numWords--;
			n = n / b;
		}
		return nums;
	}
	
	/*
	 * Perhaps future support could be added for creating more complex
	 * translations for individual parts of speech. e.g. I run a company,
	 * versus I run a marathon. Otherwise translation is limited to
	 * simple mapping between an english word and an output. This method
	 * would be useful for grabbing the possible part of speech of the word
	 * from the dictionary
	 */
	public boolean[] getPartsOfSpeech(String master) {
		boolean[] partsOfSpeech = new boolean[10];
		String arr[] = {"n", "v", "adj", "adv", "pron", "prep", "conj", "det", "exclamation", "def"};
		for(int i = 0; i < arr.length; i++) {
			if(containsString(master, " "+arr[i]+".", false) != -1) {
				partsOfSpeech[i] = true;
			}
		}
		return partsOfSpeech;
	}
	
	public int containsString(String master, String key, boolean isWord) {
		int i = 0, j = 0;
		while(i < master.length()) {
			if(isWord) {
				if(master.charAt(i) != key.charAt(j)) return -1;
				if(j == key.length()-1) return i+1 < master.length()? i+1: i;
				i++; j++;
			}
			if(master.charAt(i) != key.charAt(j)) {
				i++;
				j = 0;
				continue;
			}
			if(j == key.length()-1) return i;
			i++;
			j++;
			
		}
		return -1;
	}
	
	/*
	 * Grab actual word from dictionary line and
	 * simplify grammar from English into Squeakese
	 * TODO: Add more simplifications in the future?
	 */
	public String simplify(String s) {
		int i = 0;
		while(i < s.length() && s.charAt(i) != ' ') {
			i++;
		}
		s = s.substring(0, i);
		if(isFood(s)) return "food";
		return s;
	}
	
	/*
	 * Example of a helper function to "simplify" : converts all "food"
	 * words into a single word "food". E.g. I eat bread -> I eat food
	 */
	public boolean isFood(String s) {
		String[] arr = {"meat", "bread", "soup", "meal", "potato", "cheese", "salad", "food"};
		for(String t: arr) {
			if(t.equals(s)) return true;
		}
		return false;
	}
}
