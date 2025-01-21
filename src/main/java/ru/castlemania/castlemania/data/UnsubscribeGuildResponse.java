package ru.castlemania.castlemania.data;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UnsubscribeGuildResponse {
    long deletedGuildId;
    long newLeader;
}
