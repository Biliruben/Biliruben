package sailpoint.xml.object;

public class CertificationItemXML {

	private String _certificationId;
	private String _certificationName;
	private String _entityId;
	private String _entityTargetId;
	private String _entityIdentity;
	private String _itemId;
	private String _itemApplication;
	private String _itemAttributeName;
	private String _itemAttributeValue;
	private String _itemNativeIdentity;

	public CertificationItemXML (CertificationItemXML original) {
		if (original != null) {
			_certificationId = original.getCertificationId();
			_certificationName = original.getCertificationName();
			_entityId = original.getEntityId();
			_entityTargetId = original.getEntityTargetId();
			_entityIdentity = original.getEntityIdentity();
			_itemId = original.getItemId();
			_itemApplication = original.getItemApplication();
			_itemAttributeName = original.getItemAttributeName();
			_itemAttributeValue = original.getItemAttributeValue();
			_itemNativeIdentity = original.getItemNativeIdentity();
		}
	}

	public CertificationItemXML() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean equals(Object yours) {
		if (yours instanceof CertificationItemXML) {
			CertificationItemXML yourItem = (CertificationItemXML)yours;

			return compareWithNull (_entityTargetId, yourItem.getEntityTargetId()) &&
			compareWithNull (_itemApplication, yourItem.getItemApplication()) &&
			compareWithNull (_itemAttributeName, yourItem.getItemAttributeName()) &&
			compareWithNull (_itemAttributeValue, yourItem.getItemAttributeValue()) &&
			compareWithNull (_itemNativeIdentity, yourItem.getItemNativeIdentity());

		} else {
			return false;
		}
	}

	private boolean compareWithNull (String mine, String yours) {
		if (mine == null && yours == null) {
			// it counts
			return true;
		} else {
			if (mine != null) {
				return mine.equals(yours);
			} else {
				return false;
			}
		}
	}

	/**
	 * @return the certificationId
	 */
	public String getCertificationId() {
		return _certificationId;
	}
	/**
	 * @param certificationId the certificationId to set
	 */
	public void setCertificationId(String certificationId) {
		this._certificationId = certificationId;
	}
	/**
	 * @return the certificationName
	 */
	public String getCertificationName() {
		return _certificationName;
	}
	/**
	 * @param certificationName the certificationName to set
	 */
	public void setCertificationName(String certificationName) {
		this._certificationName = certificationName;
	}
	/**
	 * @return the entityId
	 */
	public String getEntityId() {
		return _entityId;
	}
	/**
	 * @param entityId the entityId to set
	 */
	public void setEntityId(String entityId) {
		this._entityId = entityId;
	}
	/**
	 * @return the entityTargetId
	 */
	public String getEntityTargetId() {
		return _entityTargetId;
	}
	/**
	 * @param entityTargetId the entityTargetId to set
	 */
	public void setEntityTargetId(String entityTargetId) {
		this._entityTargetId = entityTargetId;
	}
	/**
	 * @return the entityIdentity
	 */
	public String getEntityIdentity() {
		return _entityIdentity;
	}
	/**
	 * @param entityIdentity the entityIdentity to set
	 */
	public void setEntityIdentity(String entityIdentity) {
		this._entityIdentity = entityIdentity;
	}
	/**
	 * @return the itemId
	 */
	public String getItemId() {
		return _itemId;
	}
	/**
	 * @param itemId the itemId to set
	 */
	public void setItemId(String itemId) {
		this._itemId = itemId;
	}
	/**
	 * @return the itemApplication
	 */
	public String getItemApplication() {
		return _itemApplication;
	}
	/**
	 * @param itemApplication the itemApplication to set
	 */
	public void setItemApplication(String itemApplication) {
		this._itemApplication = itemApplication;
	}
	/**
	 * @return the itemAttributeName
	 */
	public String getItemAttributeName() {
		return _itemAttributeName;
	}
	/**
	 * @param itemAttributeName the itemAttributeName to set
	 */
	public void setItemAttributeName(String itemAttributeName) {
		this._itemAttributeName = itemAttributeName;
	}
	/**
	 * @return the itemAttributeValue
	 */
	public String getItemAttributeValue() {
		return _itemAttributeValue;
	}
	/**
	 * @param itemAttributeValue the itemAttributeValue to set
	 */
	public void setItemAttributeValue(String itemAttributeValue) {
		this._itemAttributeValue = itemAttributeValue;
	}
	/**
	 * @return the itemNativeIdentity
	 */
	public String getItemNativeIdentity() {
		return _itemNativeIdentity;
	}
	/**
	 * @param itemNativeIdentity the itemNativeIdentity to set
	 */
	public void setItemNativeIdentity(String itemNativeIdentity) {
		this._itemNativeIdentity = itemNativeIdentity;
	}

}
