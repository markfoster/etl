// default package
// Generated 11-Jun-2012 16:41:24 by Hibernate Tools 3.2.2.GA


import java.util.Date;

/**
 * Partner generated by hbm2java
 */
public class Partner  implements java.io.Serializable {


     private PartnerId id;
     private String name;
     private Character actionCode;
     private Date lastUpdated;

    public Partner() {
    }

	
    public Partner(PartnerId id) {
        this.id = id;
    }
    public Partner(PartnerId id, String name, Character actionCode, Date lastUpdated) {
       this.id = id;
       this.name = name;
       this.actionCode = actionCode;
       this.lastUpdated = lastUpdated;
    }
   
    public PartnerId getId() {
        return this.id;
    }
    
    public void setId(PartnerId id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    public Character getActionCode() {
        return this.actionCode;
    }
    
    public void setActionCode(Character actionCode) {
        this.actionCode = actionCode;
    }
    public Date getLastUpdated() {
        return this.lastUpdated;
    }
    
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }




}


