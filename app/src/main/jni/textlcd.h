//
// Created by DKU on 2019-06-11.
//

//#ifndef JJM_APPLICATION_TEXTLCD_H
#define JJM_APPLICATION_TEXTLCD_H

//#endif //JJM_APPLICATION_TEXTLCD_H

//#ifndef TEXTLCD_H_
#define TEXTLCD_H_

#define TEXTLCD_ON              1
#define TEXTLCD_OFF     2
#define TEXTLCD_INIT    3
#define TEXTLCD_CLEAR           4

#define TEXTLCD_LINE1           5
#define TEXTLCD_LINE2           7

struct strcommand_variable {
    char rows;
    char nfonts;
    char display_enable;
    char cursor_enable;

    char nblink;
    char set_screen;
    char set_rightshit;
    char increase;
    char nshift;
    char pos;
    char command;
    char strlenght;
    char buf[16];
};
//#endif
