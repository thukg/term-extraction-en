package test;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @desc - KeyTermFindingService
 * 
 * @author gb [elivoa AT gmail.com] ??
 * @modify gb [elivoa AT gmail.com] Sep 1, 2009
 * 
 */
public class KeyTermFindingService {

	private static KeyTermFindingService instance;

	public static KeyTermFindingService getInstance() {
		if (null == instance) {
			instance = new KeyTermFindingService();
		}
		return instance;
	}

	private String serveraddr = "localhost";
	private int port = 6060;
	private int timeout = 3000;

	public KeyTermFindingService() {
		super();
	}
	
	public KeyTermFindingService(String ip, int port, int timeout) {
		super();
		this.serveraddr = ip;
		this.port = port;
		this.timeout = timeout;
	}

	public List<String> getKeyTermsFromString(int retryTimes, String instr) {
		return getTerms(retryTimes, instr);
	}

	public List<String> extractKeyTerms(int retryTimes, String... inputs) {
		return extractKeyTerms(retryTimes, 5, inputs);
	}

	public List<String> extractKeyTerms(int retryTimes, int num, String... inputs) {
		if (null == inputs || inputs.length == 0) {
			Collections.emptyList();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<cmd>\n" + "num=" + num + "\n" + "type=PhraseOnly\n" + "</cmd>");
		for (String input : inputs) {
			sb.append(input);
			sb.append(". ");
		}
		return getTerms(retryTimes, sb.toString());
	}

	public List<String> extractKeyTerms(int retryTimes, int num, List<String> pubTitles) {
		String[] titles = pubTitles.toArray(new String[pubTitles.size()]);
		return extractKeyTerms(num, titles);
	}

	protected List<String> getTerms(int retryTimes, String instr) {
		return getTerms(retryTimes, instr, true);
	}

	protected List<String> getTerms(int retryTimes, String instr, boolean ispostprocess) {
		List<String> listRI = new ArrayList<String>();
		if (retryTimes <= 0) {
			retryTimes = 1;
		}
		boolean success = false;
		// Create a socket with a timeout
		while (retryTimes-- > 0 && !success) {
			try {
				InetAddress addr = InetAddress.getByName(serveraddr);
				SocketAddress sockaddr = new InetSocketAddress(addr, port);

				// Create an unbound socket
				Socket sock = new Socket();

				// This method will block no more than timeoutMs.
				// If the timeout occurs, SocketTimeoutException is thrown.
				int timeoutMs = timeout; // 2 seconds
				sock.connect(sockaddr, timeoutMs);

				// submit the text content to the term extraction server
				BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
				wr.write(instr + " <end>\n");
				wr.flush();

				// read the extraction results
				BufferedReader rd = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				String str;
				while ((str = rd.readLine()) != null) {
					listRI.add(str);
				}
				rd.close();

				if (ispostprocess) {
					listRI = postprocess(listRI);
				}
				success = true;
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
			} catch (Exception e) {
				StringBuilder sb = new StringBuilder();
				sb.append("Error: KeyTermFindingService:");
				sb.append(String.format("[Server:%s:%d, timeout: %d]", this.serveraddr, this.port,
						this.timeout));
				sb.append(", ").append(e.getLocalizedMessage()).append(".");
				System.err.println(sb.toString());
			}
		}
		return listRI;
	}

	protected List<String> postprocess(List<String> list) {
		List<String> liRet = new ArrayList<String>();

		String tmp;
		for (int i = 0; i < list.size(); i++) {
			tmp = list.get(i);
			String[] tmplist = tmp.split("\t");
			liRet.add(tmplist[0]);
		}
		return liRet;
	}

	/**
	 * SelfTest
	 */
	public static void main(String[] args) {
		KeyTermFindingService s = KeyTermFindingService.getInstance();
		List<String> keyTermsByString = s
				.extractKeyTerms(
						1,
						10,
						"	Important Dates"
								+ "	Submissions"
								+ "	Workshop Chairs"
								+ "	Program Committee"
								+ "	Contact us"
								+ "		 "
								+ "			"
								+ "	Objectives"
								+ "	The workshop aims to discuss key issues of searching and mining a special kind of increasingly important sources: Social Web and Social Networks (SWN)."
								+ "	There are a growing number of highly-popular user-centric applications, especially with the popularity of the Web 2.0. Such examples include blogs, folksonomies, wikis and Web communities in specific topics such as in academic research area. They have formed a new Web, Social Web and further formed social networks. SWN generates a lot of structured and semi-structured information. This information greatly enlarges the content of Web. At the same time, it introduces many interesting research issues (e.g., social web storage, search and mining, social network building, expertise oriented search and association search in social networks) and as well many real-world applications (e.g. web community detection and search, hot-topic detection in a specific web community). These research issues have been receiving in the recent years growing attentions."
								+ "	This workshop solicits contributions on SWN search and mining including Webbased and Semantic Web-based social applications, the emerging applications of the Web as a social medium such as its typical application in the academic area. Workshop Papers will elaborate related methods, issues associated to SWN extraction, storage, search, and mining."
								+ "	Invited Speaker"
								+ "	To be available soon..."
								+ "	Topics of Interests"
								+ "	The workshop will provide a forum for researchers from all over the world to share information on their latest investigations in SWN search, mining and its application particularly in academic research area."
								+ "	The broader context of the workshop can be related in some respects to the areas of Web Mining, Social Networks Analysis, Semantic Web, Information Retrieval, and Natural Language Processing. In addition to paper presentations and depending on time limitations, we will solicit an invited talk or a panel that will stress the interdisciplinary challenges of SWN search and mining."
								+ "	Topics in SWN search and mining of interest include but are not limited to:"
								+ "	    * Algorithms for SWN search"
								+ "	    * Personalized search for social interaction"
								+ "	    * Classification, clustering and summarization on SWN"
								+ "	    * Topic detection and topic trend analysis"
								+ "	    * Events/collaborator recommendation"
								+ "	    * Name disambiguation and normalization"
								+ "	    * Collaborative filtering and recommender systems"
								+ "	    * Social network modeling and analysis"
								+ "	    * Search algorithms in large-scale social networks"
								+ "	    * Evolution of social networks"
								+ "	    * Social network extraction"
								+ "	    * Temporal analysis on SWN topologies"
								+ "	    * Temporal analysis on SWN's topologies"
								+ "	    * Applications of SWN"
								+ "	    * Integration of heterogeneous SWNs"
								+ "	    * Search across heterogeneous social networks"
								+ "	    * Semantic social networks"
								+ "	    * Privacy and security issues in SWN"
								+ "	    * Large-scal surveys or studies of SWN    "
								+ "	Important Dates"
								+ "	    * Submission Deadline: July 20, 2009"
								+ "	    * Notification of Acceptance: Auguest 10, 2009"
								+ "	    * Camera Ready Due: Auguest 25th, 2009"
								+ "	    * Workshop Date: November 2, 2009"
								+ "	 "
								+ "	Submissions"
								+ "	Papers should be no longer than 8 pages, including all references and figures. Papers should be formatted using the ACM camera-ready templates, which can be found at: ACM camera-ready template."
								+ "	All papers must be submitted in either Adobe Portable Document Format (PDF). Please ensure that any special fonts used are included in the submitted documents. Please use the following link to submit your paper: Easychair Submission System for SWSM2009. "
								+ "	 "
								+ "	Workshop Chairs"
								+ "	    * Irwin King. The Chinese University of Hong Kong, China, king@cse.cuhk.edu.hk"
								+ "	    * Juanzi Li. Tsinghua University, China, ljz@keg.cs.tsinghua.edu.cn"
								+ "	    * Gui-Rong Xue. Shanghai Jiao Tong University, China, grxue@apex.sjtu.edu.cn"
								+ "	    * Jie Tang. Tsinghua University, China, tangjie@keg.cs.tsinghua.edu.cn "
								+ "	 "
								+ "	Program Committee"
								+ "	    * Harold Boley, Institute for Information Technology - e-Business of NRC, Canada"
								+ "	    * Ling Chen, L3S Research Center, German"
								+ "	    * Mingmin Chi, Fudan University, China"
								+ "	    * Isaac Councill, the Pennsylvania State University, USA"
								+ "	    * Stefan Decker, DERI galway, Ireland"
								+ "	    * Li Ding, Rensselaer Polytechnic Institute, USA"
								+ "	    * Dingyi Han, Shanghai Jiao Tong University, China"
								+ "	    * Yutaka Matsuo, National Institute of Advanced Industrial Science and Technology, Japan"
								+ "	    * Andrew McCallum, University of Massachusetts, USA"
								+ "	    * Zaiqing Nie, Microsoft Research Asia, China"
								+ "	    * Yue Pan, IBM China Research Lab, China"
								+ "	    * Zhiyong Peng, Wuhan University, China"
								+ "	    * Charles Petrie, Standford University, USA"
								+ "	    * Dou Shen, Microsoft, USA"
								+ "	    * Vaclav Snasel, VSB-Technical University of Ostrava, CZ"
								+ "	    * Zhong Su, IBM China Research Lab, China"
								+ "	    * Wensi Xi, Google, USA"
								+ "	    * Qiang Yang, Hong Kong University of Science and Technology, China"
								+ "	    * Huajun Zeng, Microsoft Research Asia, China"
								+ "	    * Hongyuan Zha, Georgia Institute of Technology, USA " + "	 "
								+ "	Contact us" + "	Jie Tang"
								+ "	1-308, FIT Building, Tsinghua University, Beijing, 100084. China"
								+ "	Phone: +8610-62788788-18" + "	Fax: +8610-62789831"
								+ "	Email: swsm2009@easychair.org"
								+ "	HP: http://keg.cs.tsinghua.edu.cn/persons/tj/" + "	 " + "		  	 "
								+ "	  	  	  	  	  	 " + "	  	  	  	"
								+ "	Maintained by KEG, Tsinghua University");
		for (String string : keyTermsByString) {
			System.out.println(string);
		}
	}
}
