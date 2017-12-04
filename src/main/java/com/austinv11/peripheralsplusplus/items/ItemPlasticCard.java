package com.austinv11.peripheralsplusplus.items;

import com.austinv11.peripheralsplusplus.reference.Reference;

public class ItemPlasticCard extends ItemPPP {
    public static final String NAME_RFID = "item.peripheralsplusone:plastic_card.name_rfid";
    public static final String NAME_NFC = "item.peripheralsplusone:plastic_card.name_nfc";
    public static final String NAME_MAG = "item.peripheralsplusone:plastic_card.name_mag";

    public ItemPlasticCard() {
        super();
        this.setRegistryName(Reference.MOD_ID, "plastic_card");
        this.setUnlocalizedName("plastic_card");
    }
}
