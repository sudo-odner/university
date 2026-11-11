package com.example.finance.data;
import androidx.annotation.NonNull;
import androidx.room.*;
@Entity(tableName="account_movements", foreignKeys={@ForeignKey(entity=Account.class,parentColumns="id",childColumns="sourceId",onDelete=ForeignKey.RESTRICT),@ForeignKey(entity=Account.class,parentColumns="id",childColumns="destinationId",onDelete=ForeignKey.RESTRICT)}, indices={@Index("sourceId"),@Index("destinationId")})
public class AccountMovement {
 @PrimaryKey(autoGenerate=true) public long id;
 public Long sourceId, destinationId;
 public long amount, createdAt;
 @NonNull public String type="";
}
