<?php
/**
 * Plugin Name: Medical Care Imaging Embed
 * Description: 医療画像AI認識UIをiframeで埋め込むショートコード [medicalcare_imaging]
 * Version: 1.0.0
 * Author: MedicalCare
 */

if (!defined('ABSPATH')) {
    exit;
}

function medicalcare_imaging_shortcode($atts) {
    $atts = shortcode_atts(array(
        'url' => getenv('MEDICALCARE_IMAGING_URL') ?: 'http://localhost:3001/imaging',
        'height' => '900',
    ), $atts, 'medicalcare_imaging');

    $url = esc_url($atts['url']);
    $height = intval($atts['height']);

    return sprintf(
        '<div class="medicalcare-imaging-embed" style="width:100%%;min-height:%dpx;">
            <iframe src="%s" style="width:100%%;height:%dpx;border:0;border-radius:8px;"
                    title="医療画像AI認識" loading="lazy" allow="clipboard-write"></iframe>
         </div>',
        $height,
        $url,
        $height
    );
}
add_shortcode('medicalcare_imaging', 'medicalcare_imaging_shortcode');
