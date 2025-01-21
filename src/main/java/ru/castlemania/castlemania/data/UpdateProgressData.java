package ru.castlemania.castlemania.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProgressData {

   private Boolean passed;
   private Integer tryNumber;
   private Double progress;
   private Integer experience;
}
