// default package
// Generated 11-Jun-2012 16:41:24 by Hibernate Tools 3.2.2.GA


import java.util.Date;

/**
 * ServiceType generated by hbm2java
 */
public class ServiceType  implements java.io.Serializable {


     private ServiceTypeId id;
     private Character actionCode;
     private Date lastUpdated;

    public ServiceType() {
    }

	
    public ServiceType(ServiceTypeId id) {
        this.id = id;
    }
    public ServiceType(ServiceTypeId id, Character actionCode, Date lastUpdated) {
       this.id = id;
       this.actionCode = actionCode;
       this.lastUpdated = lastUpdated;
    }
   
    public ServiceTypeId getId() {
        return this.id;
    }
    
    public void setId(ServiceTypeId id) {
        this.id = id;
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


