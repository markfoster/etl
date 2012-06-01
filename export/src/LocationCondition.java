// default package
// Generated 28-May-2012 14:25:40 by Hibernate Tools 3.2.2.GA


import java.util.Date;

/**
 * LocationCondition generated by hbm2java
 */
public class LocationCondition  implements java.io.Serializable {


     private LocationConditionId id;
     private Character type;
     private String text;
     private String reason;
     private Character actionCode;
     private Date lastUpdated;

    public LocationCondition() {
    }

	
    public LocationCondition(LocationConditionId id) {
        this.id = id;
    }
    public LocationCondition(LocationConditionId id, Character type, String text, String reason, Character actionCode, Date lastUpdated) {
       this.id = id;
       this.type = type;
       this.text = text;
       this.reason = reason;
       this.actionCode = actionCode;
       this.lastUpdated = lastUpdated;
    }
   
    public LocationConditionId getId() {
        return this.id;
    }
    
    public void setId(LocationConditionId id) {
        this.id = id;
    }
    public Character getType() {
        return this.type;
    }
    
    public void setType(Character type) {
        this.type = type;
    }
    public String getText() {
        return this.text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    public String getReason() {
        return this.reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
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


