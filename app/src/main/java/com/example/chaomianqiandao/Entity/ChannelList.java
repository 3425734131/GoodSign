
package com.example.chaomianqiandao.Entity;

public class ChannelList {

    private int cfid;
    private long norder;
    private String cataName;
    private String cataid;
    private long id;
    private long cpi;
    private String key;
    private Content content;
    private int topsign;
    public void setCfid(int cfid) {
         this.cfid = cfid;
     }
     public int getCfid() {
         return cfid;
     }

    public void setNorder(long norder) {
         this.norder = norder;
     }
     public long getNorder() {
         return norder;
     }

    public void setCataName(String cataName) {
         this.cataName = cataName;
     }
     public String getCataName() {
         return cataName;
     }

    public void setCataid(String cataid) {
         this.cataid = cataid;
     }
     public String getCataid() {
         return cataid;
     }

    public void setId(long id) {
         this.id = id;
     }
     public long getId() {
         return id;
     }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setCpi(long cpi) {
         this.cpi = cpi;
     }
     public long getCpi() {
         return cpi;
     }


    public void setContent(Content content) {
         this.content = content;
     }
     public Content getContent() {
         return content;
     }

    public void setTopsign(int topsign) {
         this.topsign = topsign;
     }
     public int getTopsign() {
         return topsign;
     }

}