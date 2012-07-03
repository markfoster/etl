//package org.cqc;
// Generated 24-May-2012 12:01:54 by Hibernate Tools 3.2.2.GA


import java.util.Date;

/**
 * Provider_Condition generated by hbm2java
 */
public class Provider_Condition  implements java.io.Serializable, CQC_Entity {

     private String providerId;
     private String conditionId;
     private String regulatedActivityNumber;
     private Character type;
     private String text;
     private String reason;
     private Character actionCode;
     private Date lastUpdated;

    public Provider_Condition() {
    }
	
    public Provider_Condition(String providerId, String regulatedActivityNumber, String conditionId) {
        this.providerId = providerId;
        this.regulatedActivityNumber = regulatedActivityNumber;
       this.conditionId = conditionId;
    }
    public Provider_Condition(String providerId, String conditionId, String regulatedActivityNumber, Character type, String text, String reason, Character actionCode, Date lastUpdated) {
       this.providerId = providerId;
       this.conditionId = conditionId;
       this.regulatedActivityNumber = regulatedActivityNumber;
       this.type = type;
       this.text = text;
       this.reason = reason;
       this.actionCode = actionCode;
       this.lastUpdated = lastUpdated;
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
 
    public String getRegulatedActivityNumber() {
        return this.regulatedActivityNumber;
    }
    
    public void setRegulatedActivityNumber(String regulatedActivityNumber) {
        this.regulatedActivityNumber = regulatedActivityNumber;
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

        public boolean equals(Object other) {
                if ((this == other))
                        return true;
                if ((other == null))
                        return false;
                if (!(other instanceof Provider_Condition))
                        return false;
                Provider_Condition castOther = (Provider_Condition) other;

                return ((this.getProviderId() == castOther.getProviderId()) || (this.getProviderId() != null && castOther.getProviderId() != null && this.getProviderId().equals(castOther.getProviderId())))
                    && ((this.getRegulatedActivityNumber() == castOther.getRegulatedActivityNumber()) || (this.getRegulatedActivityNumber() != null && castOther.getRegulatedActivityNumber() != null && this.getRegulatedActivityNumber().equals(castOther.getRegulatedActivityNumber())))
                    && ((this.getConditionId() == castOther.getConditionId()) || (this.getConditionId() != null && castOther.getConditionId() != null && this.getConditionId().equals(castOther.getConditionId())));
        }

        public int hashCode() {
                int result = 17;

                result = 37
                                * result
                                + (getProviderId() == null ? 0 : this.getProviderId()
                                                .hashCode());
                result = 37
                                * result
                                + (getRegulatedActivityNumber() == null ? 0 : this.getRegulatedActivityNumber()
                                                .hashCode());
                result = 37
                                * result
                                + (getConditionId() == null ? 0 : this.getConditionId()
                                                .hashCode());
                return result;
        }

   public String getPK() {
         return String.format("%s/%s/%s", getProviderId(), 
                                          getRegulatedActivityNumber(), getConditionId());
   }

}


