// default package
// Generated 11-Jun-2012 16:41:24 by Hibernate Tools 3.2.2.GA



/**
 * ProviderConditionId generated by hbm2java
 */
public class ProviderConditionId  implements java.io.Serializable {


     private String providerId;
     private String conditionId;

    public ProviderConditionId() {
    }

    public ProviderConditionId(String providerId, String conditionId) {
       this.providerId = providerId;
       this.conditionId = conditionId;
    }
   
    public String getProviderId() {
        return this.providerId;
    }
    
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
    public String getConditionId() {
        return this.conditionId;
    }
    
    public void setConditionId(String conditionId) {
        this.conditionId = conditionId;
    }


   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof ProviderConditionId) ) return false;
		 ProviderConditionId castOther = ( ProviderConditionId ) other; 
         
		 return ( (this.getProviderId()==castOther.getProviderId()) || ( this.getProviderId()!=null && castOther.getProviderId()!=null && this.getProviderId().equals(castOther.getProviderId()) ) )
 && ( (this.getConditionId()==castOther.getConditionId()) || ( this.getConditionId()!=null && castOther.getConditionId()!=null && this.getConditionId().equals(castOther.getConditionId()) ) );
   }
   
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + ( getProviderId() == null ? 0 : this.getProviderId().hashCode() );
         result = 37 * result + ( getConditionId() == null ? 0 : this.getConditionId().hashCode() );
         return result;
   }   


}


