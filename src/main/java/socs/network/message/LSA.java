package socs.network.message;

import java.io.Serializable;
import java.util.*;

public class LSA implements Serializable {

  public class LSAComparator implements Comparator<LinkDescription>, Serializable {

    @Override
    public int compare(LinkDescription o1, LinkDescription o2) {
      return o1.linkID.compareTo(o2.linkID);
    }

    @Override
    public boolean equals(Object obj) {
      return obj.equals(obj);
    }
  }

  //IP address of the router originate this LSA
  public String linkStateID;
  public int lsaSeqNumber;

  public Set<LinkDescription> links = new TreeSet<>(new LSAComparator());

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(linkStateID + ":").append(lsaSeqNumber + "\n");
    for (LinkDescription ld : links) {
      sb.append(ld);
      sb.append("; ");
    }
    sb.append("\n");
    return sb.toString();
  }
}
