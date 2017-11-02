import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.Topological;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class WordNet {

    private final Digraph hypernymsGraph;
    private HashMap<String, Integer> nouns = new HashMap<>();
    private HashMap<Integer, String> originalSynsets = new HashMap<>();

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        In synsetsStream = new In(synsets);
        int vertexCount = 0;
        while (synsetsStream.hasNextLine()) {
            String[] segments = synsetsStream.readLine().split(",");
            int synsetId = Integer.parseInt(segments[0]);
            vertexCount++;
            originalSynsets.put(synsetId, segments[1]);
            List<String> synonyms = Arrays.asList(segments[1].split(" "));
            for (String synonym : synonyms) {
                nouns.put(synonym, synsetId);
            }
        }

        hypernymsGraph = new Digraph(vertexCount);
        In hypernymsStream = new In(hypernyms);
        while (hypernymsStream.hasNextLine()) {
            String[] segments = hypernymsStream.readLine().split(",");
            int v = Integer.parseInt(segments[0]);
            for (int i = 1; i < segments.length; i++) {
                int w = Integer.parseInt(segments[i]);
                hypernymsGraph.addEdge(v, w);
            }
        }
        DepthFirstOrder dfo = new DepthFirstOrder(hypernymsGraph);
        Topological tg = new Topological(hypernymsGraph);
        if(!tg.hasOrder()){
            throw new IllegalArgumentException("hypernymsGraph has cycle.");
        }
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return nouns.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) {
            throw new IllegalArgumentException();
        }
        return nouns.containsKey(word);
    }

    // distance between nounA and nounB
    public int distance(String nounA, String nounB) {
        if (isNoun(nounA) && isNoun(nounB)) {
            int nounAId = nouns.get(nounA);
            int nounBId = nouns.get(nounB);
            SAP sap = new SAP(hypernymsGraph);
            return sap.length(nounAId, nounBId);
        } else {
            throw new IllegalArgumentException();
        }
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path
    //  An ancestral path between two vertices v and w in a digraph
    //  is a directed path from v to a common ancestor x,
    //  together with a directed path from w to the same ancestor x.
    //  A shortest ancestral path is an ancestral path of minimum total length.
    public String sap(String nounA, String nounB) {
        if (isNoun(nounA) && isNoun(nounB)) {
            int nounAId = nouns.get(nounA);
            int nounBId = nouns.get(nounB);
            SAP sap = new SAP(hypernymsGraph);
            return originalSynsets.get(sap.ancestor(nounAId, nounBId));
        } else {
            throw new IllegalArgumentException();
        }
    }

    // do unit testing of this class
    public static void main(String[] args) {
    }

    public class DepthFirstOrder {
        private boolean foundRoot = false;
        private boolean[] marked;
        private Stack<Integer> reversePost;

        DepthFirstOrder(Digraph G) {
            reversePost = new Stack<Integer>();
            marked = new boolean[G.V()];
            for (int v = 0; v < G.V(); v++) {
                if (G.outdegree(v) == 0) {
                    if (foundRoot) {
                        throw new IllegalArgumentException("More than one root.");
                    }
                    foundRoot = true;
                }
                if (!marked[v]) {
                    dfs(G, v);
                }
            }
        }

        private void dfs(Digraph G, int v) {
            marked[v] = true;
            for (int w : G.adj(v)) {
                if (!marked[w]) {
                    dfs(G, w);
                }
            }
            reversePost.push(v);
        }

        public Iterable<Integer> reversePost() {
            return reversePost;
        }
    }
}