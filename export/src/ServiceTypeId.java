// default package
// Generated 11-Jun-2012 16:41:24 by Hibernate Tools 3.2.2.GA



/**
 * ServiceTypeId generated by hbm2java
 */
public class ServiceTypeId  implements java.io.Serializable {


     private String providerId;
     private String locationId;
     private String serviceTypeId;

    public ServiceTypeId() {
    }

    public ServiceTypeId(String providerId, String locationId, String serviceTypeId) {
       this.providerId = providerId;
       this.locationId = locationId;
       this.serviceTypeId = serviceTypeId;
    }
   
    public String getProviderId() {
        return this.providerId;
    }
    
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
    public String getLocationId() {
        return this.locationId;
    }
    
    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }
    public String getServiceTypeId() {
        return this.serviceTypeId;
    }
    
    public void setServiceTypeId(String serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }


   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof ServiceTypeId) ) return false;
		 ServiceTypeId castOther = ( ServiceTypeId ) other; 
         
		 return ( (this.getProviderId()==castOther.getProviderId()) || ( this.getProviderId()!=null && castOther.getProviderId()!=null && this.getProviderId().equals(castOther.getProviderId()) ) )
 && ( (this.getLocationId()==castOther.getLocationId()) || ( this.getLocationId()!=null && castOther.getLocationId()!=null && this.getLocationId().equals(castOther.getLocationId()) ) )
 && ( (this.getServiceTypeId()==castOther.getServiceTypeId()) || ( this.getServiceTypeId()!=null && castOther.getServiceTypeId()!=null && this.getServiceTypeId().equals(castOther.getServiceTypeId()) ) );
   }
   
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + ( getProviderId() == null ? 0 : this.getProviderId().hashCode() );
         result = 37 * result + ( getLocationId() == null ? 0 : this.getLocationId().hashCode() );
         result = 37 * result + ( getServiceTypeId() == null ? 0 : this.getServiceTypeId().hashCode() );
         return result;
   }   


}


