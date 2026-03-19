package poly.edu.service;

import poly.edu.entity.dto.AboutPageDTO;

public interface AboutService {
    AboutPageDTO getAboutPage();
    void saveAboutPage(AboutPageDTO aboutPageDTO);
}