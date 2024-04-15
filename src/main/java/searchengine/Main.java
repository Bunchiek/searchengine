package searchengine;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.swing.text.html.parser.Entity;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public class Main {
    public static void main(String[] args) throws IOException {

        String test = "\n" +
                "<!DOCTYPE html>\n" +
                "<html lang=\"ru\">\n" +
                "<head>\n" +
                "\n" +
                "<!-- Yandex.Metrika counter -->\n" +
                "<script type=\"text/javascript\" >\n" +
                "   (function(m,e,t,r,i,k,a){m[i]=m[i]||function(){(m[i].a=m[i].a||[]).push(arguments)};\n" +
                "   m[i].l=1*new Date();\n" +
                "   for (var j = 0; j < document.scripts.length; j++) {if (document.scripts[j].src === r) { return; }}\n" +
                "   k=e.createElement(t),a=e.getElementsByTagName(t)[0],k.async=1,k.src=r,a.parentNode.insertBefore(k,a)})\n" +
                "   (window, document, \"script\", \"https://mc.yandex.ru/metrika/tag.js\", \"ym\");\n" +
                "\n" +
                "   ym(95227590, \"init\", {\n" +
                "        clickmap:true,\n" +
                "        trackLinks:true,\n" +
                "        accurateTrackBounce:true,\n" +
                "        webvisor:true,\n" +
                "        ecommerce:\"dataLayer\"\n" +
                "   });\n" +
                "</script>\n" +
                "<noscript><div><img src=\"https://mc.yandex.ru/watch/95227590\" style=\"position:absolute; left:-9999px;\" alt=\"\" /></div></noscript>\n" +
                "<!-- /Yandex.Metrika counter -->\n" +
                "  <meta charset=\"utf-8\">\n" +
                "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "  <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->\n" +
                "  <title>Ирина Старженецкая. Возвращение</title>  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
                "<meta name=\"robots\" content=\"index, follow\" />\n" +
                "<link href=\"/bitrix/cache/css/s1/addeo/kernel_main/kernel_main.css?168436335244417\" type=\"text/css\"  rel=\"stylesheet\" />\n" +
                "<link href=\"/bitrix/cache/css/s1/addeo/page_019626193385ed6add9190a37214ba44/page_019626193385ed6add9190a37214ba44.css?168437358333561\" type=\"text/css\"  rel=\"stylesheet\" />\n" +
                "<link href=\"/bitrix/cache/css/s1/addeo/template_1974cebda3000f2fad3168e32c750bb1/template_1974cebda3000f2fad3168e32c750bb1.css?1684363327729\" type=\"text/css\"  data-template-style=\"true\"  rel=\"stylesheet\" />\n" +
                "<script type=\"text/javascript\">if(!window.BX)window.BX={message:function(mess){if(typeof mess=='object') for(var i in mess) BX.message[i]=mess[i]; return true;}};</script>\n" +
                "<script type=\"text/javascript\">(window.BX||top.BX).message({'JS_CORE_LOADING':'Загрузка...','JS_CORE_NO_DATA':'- Нет данных -','JS_CORE_WINDOW_CLOSE':'Закрыть','JS_CORE_WINDOW_EXPAND':'Развернуть','JS_CORE_WINDOW_NARROW':'Свернуть в окно','JS_CORE_WINDOW_SAVE':'Сохранить','JS_CORE_WINDOW_CANCEL':'Отменить','JS_CORE_WINDOW_CONTINUE':'Продолжить','JS_CORE_H':'ч','JS_CORE_M':'м','JS_CORE_S':'с','JSADM_AI_HIDE_EXTRA':'Скрыть лишние','JSADM_AI_ALL_NOTIF':'Показать все','JSADM_AUTH_REQ':'Требуется авторизация!','JS_CORE_WINDOW_AUTH':'Войти','JS_CORE_IMAGE_FULL':'Полный размер'});</script>\n" +
                "<script type=\"text/javascript\">(window.BX||top.BX).message({'LANGUAGE_ID':'ru','FORMAT_DATE':'DD.MM.YYYY','FORMAT_DATETIME':'DD.MM.YYYY HH:MI:SS','COOKIE_PREFIX':'BITRIX_SM','SERVER_TZ_OFFSET':'10800','SITE_ID':'s1','SITE_DIR':'/','USER_ID':'','SERVER_TIME':'1713113421','USER_TZ_OFFSET':'0','USER_TZ_AUTO':'Y','bitrix_sessid':'bfd7aff2e4d895dc534ebb136ef1df54'});</script>\n" +
                "\n" +
                "\n" +
                "<script type=\"text/javascript\" src=\"/bitrix/cache/js/s1/addeo/kernel_main/kernel_main.js?1700581240283588\"></script>\n" +
                "<script type=\"text/javascript\" src=\"/bitrix/cache/js/s1/addeo/kernel_twim.recaptchafree/kernel_twim.recaptchafree.js?17005890044685\"></script>\n" +
                "<script type=\"text/javascript\" src=\"https://www.google.com/recaptcha/api.js?onload=onloadRecaptchafree&render=explicit&hl=ru\"></script>\n" +
                "<script type=\"text/javascript\" src=\"//www.google.com/recaptcha/api.js\"></script>\n" +
                "<script type=\"text/javascript\">BX.setJSList(['/bitrix/js/main/core/core.js?168436311173480','/bitrix/js/main/core/core_ajax.js?170058021135602','/bitrix/js/main/json/json2.min.js?16843631133467','/bitrix/js/main/core/core_ls.js?16843631127365','/bitrix/js/main/session.js?16843631132511','/bitrix/js/main/core/core_window.js?168436311274754','/bitrix/js/main/core/core_popup.js?168436311229812','/bitrix/js/main/core/core_date.js?168436311234241','/bitrix/js/main/utils.js?168436311319858','/bitrix/js/twim.recaptchafree/script.js?17005889834421','/bitrix/templates/addeo/components/bitrix/catalog/news/bitrix/catalog.element/.default/script.js?148050852754375']); </script>\n" +
                "<script type=\"text/javascript\">BX.setCSSList(['/bitrix/js/main/core/css/core.css?16843631122854','/bitrix/js/main/core/css/core_popup.css?168436311229699','/bitrix/js/main/core/css/core_date.css?16843631129657','/bitrix/templates/addeo/components/bitrix/catalog/news/style.css?1480508527697','/bitrix/templates/addeo/components/bitrix/catalog/news/bitrix/catalog.element/.default/style.css?148050852728758','/bitrix/templates/addeo/components/bitrix/menu/top/style.css?1480508527490']); </script>\n" +
                "\n" +
                "\n" +
                "<script type=\"text/javascript\" src=\"/bitrix/cache/js/s1/addeo/page_5a61f492ed07991e19675cff7da0b36d/page_5a61f492ed07991e19675cff7da0b36d.js?168437358355056\"></script>\n" +
                "<script type=\"text/javascript\">var _ba = _ba || []; _ba.push([\"aid\", \"75edb33fc1ac88e4b848dce98874d580\"]); _ba.push([\"host\", \"nikoartgallery.com\"]); (function() {var ba = document.createElement(\"script\"); ba.type = \"text/javascript\"; ba.async = true;ba.src = (document.location.protocol == \"https:\" ? \"https://\" : \"http://\") + \"bitrix.info/ba.js\";var s = document.getElementsByTagName(\"script\")[0];s.parentNode.insertBefore(ba, s);})();</script>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <!-- Bootstrap -->\n" +
                "  <link href=\"/bitrix/templates/addeo/css/bootstrap.css\" rel=\"stylesheet\">\n" +
                "  <link href=\"/bitrix/templates/addeo/css/animate.css\" rel=\"stylesheet\">\n" +
                "  <link href=\"/bitrix/templates/addeo/css/owl.carousel.css\" rel=\"stylesheet\">\n" +
                "  <link href=\"/bitrix/templates/addeo/css/lightbox.css\" rel=\"stylesheet\">\n" +
                "<link href=\"/bitrix/templates/addeo/css/orders.css\" rel=\"stylesheet\">\n" +
                "  <link href=\"/bitrix/templates/addeo/css1/style.css\" rel=\"stylesheet\">\n" +
                "  <!--<link href=\"/bitrix/templates/addeo/css/style.css\" rel=\"stylesheet\">-->\n" +
                "  <link href=\"/bitrix/templates/addeo/css/media.css\" rel=\"stylesheet\">\n" +
                "<link href=\"/bitrix/templates/addeo/css/font-awesome.css\" rel=\"stylesheet\">\n" +
                "\n" +
                "\n" +
                "  <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->\n" +
                "  <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->\n" +
                "    <!--[if lt IE 9]>\n" +
                "      <script src=\"https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js\"></script>\n" +
                "      <script src=\"https://oss.maxcdn.com/respond/1.4.2/respond.min.js\"></script>\n" +
                "      <![endif]-->\n" +
                "    </head>\n" +
                "    <body>\n" +
                "\n" +
                "\n" +
                "    <div id=\"panel\"></div>\n" +
                "    \n" +
                "      <header>\n" +
                "        <div class=\"row\">\n" +
                "          <div class=\"top\">\n" +
                "            <div class=\"container\">\n" +
                "              <p class=\"top-left\">\n" +
                "              \t  \t              \t\tБронирование <a href=\"/arenda/\">залов галереи</a> по телефонам <span>+7 (985) 998 97 95 / +7 (499) 253 86 07</span>\t              \n" +
                "\n" +
                "              </p>\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "                            \t\t<p class=\"top-right\"><a href=\"/en/\">In English</a></p>\n" +
                "              \n" +
                "\t\t\t\t<div class=\"head_social\">\n" +
                "<a target=\"_blank\" href=\"https://vk.com/public200017253 \"><i class=\"fa fa-vk\"></i></a>\n" +
                "<a target=\"_blank\" href=\"https://t.me/nikoartgallery\"><i class=\"fa fa-telegram\"></i></a>\n" +
                "        <!-- asocial 2\n" +
                "<a target=\"_blank\" href=\"https://www.instagram.com/niko_art_gallery/ \"><i class=\"fa fa-instagram\"></i></a>\n" +
                "<a target=\"_blank\" href=\"https://www.facebook.com/pg/artgalleryniko\"><i class=\"fa fa-facebook\"></i></a>\n" +
                "       \t\t\t-->\n" +
                "       \t\t\t</div>\n" +
                "\n" +
                "            </div>\n" +
                "          </div>\n" +
                "          <div class=\"top-mobile\">\n" +
                "<div class=\"container\">\n" +
                "            <p class=\"top-left\">\n" +
                "              \t\t              \t\tБронирование <a href=\"/arenda/\">залов галереи</a> по телефонам <span>+7 (985) 998 97 95 / +7 (499) 253 86 07</span>\t                            </p>\n" +
                "\n" +
                "\t\t\t</div>\n" +
                "          </div>\n" +
                "        </div>\n" +
                "        <div class=\"row\">\n" +
                "          <div class=\"bottom\">\n" +
                "            <div class=\"container\">\n" +
                "              <div class=\"mobile-menu\">\n" +
                "                <span class=\"icon-bar\"></span>\n" +
                "                <span class=\"icon-bar\"></span>\n" +
                "                <span class=\"icon-bar\"></span>\n" +
                "              </div>\n" +
                "              <div class=\"logo\"><a href=\"/\"><img src=\"/bitrix/templates/addeo/img/Logotip.svg\" alt=\"\"></a></div>\n" +
                "              <h1>\n" +
                "                            \t\tКреативное пространство и галерея Н.Б. Никогосяна              \n" +
                "              </h1>\n" +
                "              \n" +
                "<ul>\n" +
                "\n" +
                "\t\t\t<li><a href=\"/gallery/\">Галерея</a></li>\n" +
                "\t\t\n" +
                "\t\t\t<li><a href=\"/author/\">О художнике</a></li>\n" +
                "\t\t\n" +
                "\t\t\t<li><a href=\"/afisha/\">Aфиша</a></li>\n" +
                "\t\t\n" +
                "\t\t\t<li><a href=\"/arenda/\">Аренда залов</a></li>\n" +
                "\t\t\n" +
                "\t\t\t<li><a href=\"/news/\" class=\"selected\">Новости</a></li>\n" +
                "\t\t\n" +
                "\t\t\t<li><a href=\"/contacts/\">Контакты</a></li>\n" +
                "\t\t\n" +
                "\n" +
                "</ul>\n" +
                "            </div>\n" +
                "          </div>\n" +
                "        </div>\n" +
                "      </header>\n" +
                "\n" +
                "      <div class=\"mobile-menu-wrap\">\n" +
                "        <div class=\"container\">\n" +
                "          <img class=\"close-mobile\" src=\"/bitrix/templates/addeo/img/close-mobile.png\" alt=\"\">\n" +
                "          \n" +
                "<ul>\n" +
                "\n" +
                "\t\t\t<li><a href=\"/gallery/\">Галерея</a></li>\n" +
                "\t\t\n" +
                "\t\t\t<li><a href=\"/author/\">О художнике</a></li>\n" +
                "\t\t\n" +
                "\t\t\t<li><a href=\"/afisha/\">Aфиша</a></li>\n" +
                "\t\t\n" +
                "\t\t\t<li><a href=\"/arenda/\">Аренда залов</a></li>\n" +
                "\t\t\n" +
                "\t\t\t<li><a href=\"/news/\" class=\"selected\">Новости</a></li>\n" +
                "\t\t\n" +
                "\t\t\t<li><a href=\"/contacts/\">Контакты</a></li>\n" +
                "\t\t\n" +
                "\n" +
                "</ul>\n" +
                "         \t <p class=\"top-left\">\n" +
                "              \t\t              \t\tБронирование <a href=\"/arenda/\">залов галереи</a> по телефонам <span>+7 (985) 998 97 95 / +7 (499) 253 86 07</span>\t                            </p>\n" +
                "\t\t\t<div class=\"mobile__social_mod\">\n" +
                "\t\t\t\t              \t\t<p class=\"top-right\"><a href=\"/en/\">In English</a></p>\n" +
                "              \n" +
                "\t\t\t\t<div class=\"head_social\">\n" +
                "\n" +
                "\t\t\t\t\t<a target=\"_blank\" href=\"https://vk.com/public200017253 \"><i class=\"fa fa-vk\"></i></a>\n" +
                "\t\t\t\t\t<a target=\"_blank\" href=\"https://t.me/nikoartgallery\"><i class=\"fa fa-telegram\"></i></a>\n" +
                "\t\t\t\t<!-- asocial 1\n" +
                "\t\t\t\t\t<a target=\"_blank\" href=\"https://www.instagram.com/niko_art_gallery/ \"><i class=\"fa fa-instagram\"></i></a>\n" +
                "\t\t\t\t\t<a target=\"_blank\" href=\"https://www.facebook.com/pg/artgalleryniko\"><i class=\"fa fa-facebook\"></i></a>\n" +
                "                -->\n" +
                "       \t\t\t</div>\n" +
                "\n" +
                "\t\t\t</div>\n" +
                "        </div>\n" +
                "      </div>\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "      <section id=\"news-page\">\n" +
                "        <div class=\"container\" id=\"header_news_detal_hide\">\n" +
                "          <h1>\n" +
                "          \tНОВОСТИ И СОБЫТИЯ          </h1>\n" +
                "          <p>\n" +
                "          \t          </p>\n" +
                "\n" +
                "</div>\n" +
                "\n" +
                "\n" +
                "<style>\n" +
                "\n" +
                "#header_news_detal_hide {\n" +
                "display:none;\n" +
                "}\n" +
                "\n" +
                "</style>\n" +
                "\n" +
                "\n" +
                "<section id=\"single-news\">\n" +
                "        <div class=\"container\">\n" +
                "\n" +
                "          <div class=\"links\">\n" +
                "            <p><a href=\"/\">Главная</a> / <a href=\"/news/\">Новости</a> / <span>Ирина Старженецкая. Возвращение</span></p>\n" +
                "          </div>\n" +
                "\n" +
                "          \n" +
                "          \t\n" +
                "<p class=\"date\">  </p>\n" +
                "          <h1>Ирина Старженецкая. Возвращение</h1>\n" +
                "\n" +
                "          \t<img class=\"single-news\" src=\"/upload/iblock/27a/27a100c800ccdd0a2f2c9f8daa4bf4dd.jpeg\" alt=\"\">\n" +
                "          <img class=\"single-news-mobile\" src=\"/upload/iblock/27a/27a100c800ccdd0a2f2c9f8daa4bf4dd.jpeg\" alt=\"\">\n" +
                "\n" +
                "          <div class=\"news-descr\">\n" +
                "          \t<p style=\"text-align: justify;\">\n" +
                " <span style=\"font-family: Arial, Helvetica;\">с 13 марта в Галерее Нико&nbsp;открыта выставка работ академика Российской академии художеств -&nbsp;</span><b><span style=\"font-family: Arial, Helvetica;\">Ирины Александровны Старженецкой</span></b><span style=\"font-family: Arial, Helvetica;\">.</span><br>\n" +
                " <span style=\"font-family: Arial, Helvetica;\"> </span><span style=\"font-family: Arial, Helvetica;\"> </span><br>\n" +
                " <span style=\"font-family: Arial, Helvetica;\"> </span><span style=\"font-family: Arial, Helvetica;\">\n" +
                "\tГерой выставки, посвященной Античной Греции - легендарный&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">Парфенон —&nbsp;символ и средоточие основ цивилизации, которые из&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">Древней Греции к ХХ1 веку распространились на весь мир и&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">безоговорочно приняты всеми культурами.</span><span style=\"font-family: Arial, Helvetica;\"> </span>\n" +
                "</p>\n" +
                " <span style=\"font-family: Arial, Helvetica;\"> </span>\n" +
                "<p style=\"text-align: justify;\">\n" +
                " <span style=\"font-family: Arial, Helvetica;\">\n" +
                "\tПарфенон —&nbsp;бесспорный идеал классического искусства, соединяет в себе ценности не только архитектурного и художественного мышления, но и философии, математики, духовной жизни, вдохновляя художников и поэтов всех времен и народов, воспевающих его в своих произведениях. В 1801&nbsp;году британский посол в Константинополе Томас Брюс лорд Элджин способствовал вывозу около половины скульптур Парфенона в Лондон. Сегодня они хранятся в Британском музее. Многочисленные мелкие фрагменты, вывозившиеся «охотниками за античностью» на протяжении 19 века, находятся в различных музеях мира и частных коллекциях. Ещё в начале 1980-х гг. министр культуры Греции Мелина Меркури начала кампанию по возвращению мраморов Парфенона в Грецию.</span><br>\n" +
                " <span style=\"font-family: Arial, Helvetica;\"> </span><br>\n" +
                " <span style=\"font-family: Arial, Helvetica;\">\n" +
                "\tПарфенон сегодня —&nbsp;величайшее достояние мировой культуры, ответственность за сохранность и целостность которого разделяет международное культурное сообщество. Гармонию нельзя разделить на отдельные части, не нарушив целостности замысла. Прогрессивные деятели культуры отстаивают идею возвращения памятнику его подлинного&nbsp;архитектурно-художественного облика.</span><br>\n" +
                " <span style=\"font-family: Arial, Helvetica;\"> </span><br>\n" +
                " <span style=\"font-family: Arial, Helvetica;\"> </span>\n" +
                "</p>\n" +
                " <span style=\"font-family: Arial, Helvetica;\"> </span>\n" +
                "<hr>\n" +
                " <span style=\"font-family: Arial, Helvetica;\"> </span>\n" +
                "<p style=\"text-align: justify;\">\n" +
                " <span style=\"font-family: Arial, Helvetica;\"> </span><span style=\"font-family: Arial, Helvetica;\"> </span><b><span style=\"font-family: Arial, Helvetica;\">Ирина Александровна&nbsp;</span></b><b><span style=\"font-family: Arial, Helvetica;\">Старженецкая</span></b><span style=\"font-family: Arial, Helvetica;\"> родилась в 1943 году на станции Арысь Казахской ССР. Прошла&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">классическую академическую школу: с 1957 по 1962 год обучалась в Московском&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">академическом художественном лицее, а в 1968 году окончила Московский&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">государственный художественный институт им. В.И. Сурикова, где ее наставниками были&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">.А.Грицай, Д.Д.Жилинский, С.Н.Шильников. С 1962 по 1985 год работала художником-</span><span style=\"font-family: Arial, Helvetica;\">постановщиком в театрах Москвы, Саратова, Самары, Кемерово, Хабаровске и других&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">городов.</span><span style=\"font-family: Arial, Helvetica;\"> </span>\n" +
                "</p>\n" +
                " <span style=\"font-family: Arial, Helvetica;\"> </span>\n" +
                "<p style=\"text-align: justify;\">\n" +
                " <span style=\"font-family: Arial, Helvetica;\"> </span><span style=\"font-family: Arial, Helvetica;\"> </span><br>\n" +
                " <span style=\"font-family: Arial, Helvetica;\"> </span><span style=\"font-family: Arial, Helvetica;\">\n" +
                "\tИрина&nbsp;- яркий живописец, она принадлежит к поколению «молодых&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">художников 1970-х», которые привнесли в искусство новое видение современности.&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">Художница посвятила свое творчество простым жизненным темам, в которых воспела&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">природу и человека. Опираясь на реальные впечатления, она создает свой уникальный&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">художественный мир. Ее работы наполнены светом и воздухом, молитвенной&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">одухотворенностью и особым сакральным смыслом. От природы обладая музыкальным&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">слухом, она уподобляет живопись музыке, добиваясь в своих произведениях цветовой&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">гармонии. С 1989 года Ирина Старженецкая все чаще обращается к религиозной теме. В&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">храмовой живописи она строго соблюдает православный канон, и почти каждая икона&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">соответствует станковому полотну, выполненному в свободной, глубоко индивидуальной&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">творческой манере. Отмечая динамику и статику, соразмерность частей и целого,&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">сочетание музыкальности ритмов с благородством цветового строя, исследователи&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">говорят о том, что чувства и мысли автора находят свое продолжение в живописных&nbsp;</span><span style=\"font-family: Arial, Helvetica;\">полотнах, где она переводит канон на язык современного искусства.</span><br>\n" +
                " <span style=\"font-family: Arial, Helvetica;\"> </span><br>\n" +
                " <span style=\"font-family: Arial, Helvetica;\"> </span><br>\n" +
                " <span style=\"font-family: Arial, Helvetica;\"> </span>\n" +
                "</p>\n" +
                " <span style=\"font-family: Arial, Helvetica;\"> </span><i><span style=\"font-family: Arial, Helvetica;\">Выставка организована в сотрудничестве с Российским комитетом международной ассоциации по&nbsp;возвращению мрамора&nbsp;Парфенона</span></i><span style=\"font-family: Arial, Helvetica;\"> </span>\n" +
                "<hr>\n" +
                "<p>\n" +
                "</p>          </div>\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "          <div class=\"share\">\n" +
                "            <h3><span>Интересно?</span> Поделитесь материалом с друзьями.<br>Им полезно, нам приятно</h3>\n" +
                "            <div class=\"share-buttons\">\n" +
                "              <script src=\"https://yastatic.net/es5-shims/0.0.2/es5-shims.min.js\" async=\"async\"></script>\n" +
                "              <script src=\"https://yastatic.net/share2/share.js\" async=\"async\"></script>\n" +
                "              <div class=\"ya-share2\" data-services=\"vkontakte,facebook,odnoklassniki,twitter\" data-counter></div>\n" +
                "            </div>\n" +
                "        <a href=\"/news/\">\n" +
                "        <button class=\"btn-back\">\n" +
                "        <img src=\"/bitrix/templates/addeo/img/arrow-left.png\" alt=\"\">Обратно ко всем новостям\n" +
                "        </button>\n" +
                "        </a>\n" +
                "          </div>\n" +
                "\n" +
                "        </div>\n" +
                "\n" +
                "        <div class=\"also-news\">\n" +
                "          <div class=\"container\">\n" +
                "            <div class=\"row\">\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t  <a href=\"/gallery/\">\n" +
                "\t                <div class=\"also-news-block\">\n" +
                "\t                  <img src=\"/upload/iblock/e31/e31faf03f31d93f5faf1c393882105aa.png\" alt=\"\">\n" +
                "\t                  <h4>Галерея Niko</h4>\n" +
                "\t                  <p>Простейшая архитектурная конструкция, известная с эпохи неолита. С древних времен и до наших дней применяется во всех зданиях, перекрытых плоской или двускатной крышей.</p>\n" +
                "\t                </div>\n" +
                "\t              </a>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t  <a href=\"/gallery/\">\n" +
                "\t                <div class=\"also-news-block\">\n" +
                "\t                  <img src=\"/upload/iblock/ee6/ee6d6516039c7d47d142fa2182fa88bb.png\" alt=\"\">\n" +
                "\t                  <h4>Аренда залов</h4>\n" +
                "\t                  <p>Простейшая архитектурная конструкция, известная с эпохи неолита. С древних времен и до наших дней применяется во всех зданиях, перекрытых плоской или двускатной крышей.</p>\n" +
                "\t                </div>\n" +
                "\t              </a>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t  <a href=\"/gallery/\">\n" +
                "\t                <div class=\"also-news-block\">\n" +
                "\t                  <img src=\"/upload/iblock/d34/d3426735734b2aafeb5a1eb207bbc71f.png\" alt=\"\">\n" +
                "\t                  <h4>Новости галереи</h4>\n" +
                "\t                  <p>Простейшая архитектурная конструкция, известная с эпохи неолита. С древних времен и до наших дней применяется во всех зданиях, перекрытых плоской или двускатной крышей.</p>\n" +
                "\t                </div>\n" +
                "\t              </a>\n" +
                "\t\t\t\t\n" +
                "\n" +
                "            </div>\n" +
                "          </div>\n" +
                "        </div>\n" +
                "\n" +
                "      </section>\n" +
                "\n" +
                "\t\t<img class=\"img-bottom\" src=\"/bitrix/templates/addeo/img/rent-bottom.jpg\" alt=\"\">\n" +
                "        <img class=\"img-bottom-mobile\" src=\"/bitrix/templates/addeo/img/rent-bottom-mobile.jpg\" alt=\"\"> \n" +
                "\n" +
                "      </section>\n" +
                "\n" +
                "      <footer>\n" +
                "        <div class=\"container\">\n" +
                "          <div class=\"row\">\n" +
                "            <div class=\"footer-top\">\n" +
                "\t\t\t\t<a href=\"/\"><img src=\"/bitrix/templates/addeo/img/logo.png\" alt=\"\"></a>\n" +
                "              \n" +
                "<ul>\n" +
                "\n" +
                "\t\t\t<li><a href=\"/gallery/\">Галерея</a></li>\n" +
                "\t\t\n" +
                "\t\t\t<li><a href=\"/author/\">О художнике</a></li>\n" +
                "\t\t\n" +
                "\t\t\t<li><a href=\"/afisha/\">Aфиша</a></li>\n" +
                "\t\t\n" +
                "\t\t\t<li><a href=\"/arenda/\">Аренда залов</a></li>\n" +
                "\t\t\n" +
                "\t\t\t<li><a href=\"/news/\" class=\"selected\">Новости</a></li>\n" +
                "\t\t\n" +
                "\t\t\t<li><a href=\"/contacts/\">Контакты</a></li>\n" +
                "\t\t\n" +
                "\n" +
                "</ul>\n" +
                "            </div>\n" +
                "          </div>\n" +
                "          <div class=\"row\">\n" +
                "            <div class=\"footer-bottom\">\n" +
                "              <p>\n" +
                "              \t\n" +
                "              \t \t              \t<span style=\"color: #acacac;\">Большой Тишинский пер., д.19, строение 1. </span><br>\n" +
                "<span style=\"color: #acacac;\"> </span><span style=\"color: #acacac;\">\n" +
                "ежедневно с 14 до 19 (посещение до договоренности)</span> \n" +
                "\t                            </p>\n" +
                "              <p class=\"phone\">\n" +
                "              \t<p>\n" +
                " <span style=\"color: #acacac;\">+7 (985) 998 97 95</span><br>\n" +
                "\t<span style=\"color: #acacac;\">\n" +
                "\t+7 (499) 253 86 07 </span>\n" +
                "</p>\n" +
                "<span style=\"color: #acacac;\"> </span><br> \n" +
                "              </p>\n" +
                "                            \t<p class=\"addeo\">дизайн и разработка - <a target=\"_blank\" href=\"http://addeo.ru\">Addeo.ru</a><br>Фото: <a href=\"mailto:sysoi@mail.ru\">Борис Сысоев</a></p>\n" +
                "              \t\t\t</div>\n" +
                "          </div>\n" +
                "        </div>\n" +
                "      </footer>\n" +
                "\n" +
                "\n" +
                "      <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->\n" +
                "      <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js\"></script>\n" +
                "      <!-- Include all compiled plugins (below), or include individual files as needed -->\n" +
                "      <script src=\"/bitrix/templates/addeo/js/bootstrap.min.js\"></script>\n" +
                "      <script src=\"/bitrix/templates/addeo/js/owl.carousel.js\"></script>\n" +
                "      <script src=\"/bitrix/templates/addeo/js/lightbox.js\"></script>\n" +
                "      <script src=\"/bitrix/templates/addeo/js/wow.min.js\"></script>\n" +
                "   \t\t<script>\n" +
                "      \t\tnew WOW().init();\n" +
                "    \t</script>\n" +
                "    \t<script src=\"/bitrix/templates/addeo/js/jquery.stellar.js\"></script>\n" +
                "      <script src=\"/bitrix/templates/addeo/js/script.js\"></script>\n" +
                "      \n" +
                "      \n" +
                "      \n" +
                "      <!— Yandex.Metrika counter —> \n" +
                "<script type=\"text/javascript\"> \n" +
                "(function (d, w, c) { \n" +
                "(w[c] = w[c] || []).push(function() { \n" +
                "try { \n" +
                "w.yaCounter44647948 = new Ya.Metrika({ \n" +
                "id:44647948, \n" +
                "clickmap:true, \n" +
                "trackLinks:true, \n" +
                "accurateTrackBounce:true, \n" +
                "webvisor:true \n" +
                "}); \n" +
                "} catch(e) { } \n" +
                "}); \n" +
                "\n" +
                "var n = d.getElementsByTagName(\"script\")[0], \n" +
                "s = d.createElement(\"script\"), \n" +
                "f = function () { n.parentNode.insertBefore(s, n); }; \n" +
                "s.type = \"text/javascript\"; \n" +
                "s.async = true; \n" +
                "s.src = \"https://mc.yandex.ru/metrika/watch.js\"; \n" +
                "\n" +
                "if (w.opera == \"[object Opera]\") { \n" +
                "d.addEventListener(\"DOMContentLoaded\", f, false); \n" +
                "} else { f(); } \n" +
                "})(document, window, \"yandex_metrika_callbacks\"); \n" +
                "</script> \n" +
                "<noscript><div><img src=\"https://mc.yandex.ru/watch/44647948\" style=\"position:absolute; left:-9999px;\" alt=\"\" /></div></noscript> \n" +
                "<!— /Yandex.Metrika counter —>\n" +
                "      \n" +
                "    </body>\n" +
                "    </html> ";

        test = Jsoup.clean(test, Safelist.none());
        int start = test.indexOf("творчество");
        int finish = test.indexOf("творчество");
        for(;;){
            if(test.charAt(start - 1) == '.' || test.charAt(start - 1) == '>'){
                break;

            }
            start --;
        }
        for(;;){
            if(test.charAt(finish + 1) == '.' ){
                finish = finish + 2;
                break;
            }
            finish ++;
        }
        test = test.substring(start, finish).replaceAll("[^А-Я а-я.,]", "");
        System.out.println(test);


//        System.out.println(test2);
//
//        System.out.println(test3);
//
//
//        String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};
//
//        String text = "https://et-cetera.ru/mobile/performance/";

//        ;
//        System.out.println(luceneMorph.checkString(test));
//        System.out.println(luceneMorph.getMorphInfo(test));

//        URL url = new URL(text);
//
//        System.out.println(url.getProtocol() + "://" + url.getHost() +"/");
//        System.out.println(((double) 23/100) * 90);







//        String result = Jsoup.clean(text, Safelist.none());
//        result = result.replaceAll("[^А-Яа-я]", " ").replaceAll("\\s{2,}", " ");


//        Map<String, Long> map = Stream.of(result.split(" "))
//                .map(String::toLowerCase)
//                .filter(s -> {
//                    for (String string : particlesNames) {
//                        if (luceneMorph.getMorphInfo(s).toString().contains(string)) {
//                            return false;
//                        }
//                    }
//                    return true;
//                })
//                .map(s->luceneMorph.getNormalForms(s).get(0))
//                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
//
//        for(Map.Entry<String,Long> entry : map.entrySet()){
//            System.out.println(entry);
//        }

    }
}

