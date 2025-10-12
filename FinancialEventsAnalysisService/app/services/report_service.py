from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import cm
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer, PageBreak, Image
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from datetime import datetime
import matplotlib.pyplot as plt
import matplotlib
matplotlib.use('Agg')  # Backend bez GUI
import io
import os
import logging
from typing import List, Optional, Dict
from app.services.influx_service import influx_service

logger = logging.getLogger(__name__)


class ReportService:
    """
    Servis za generisanje PDF izveštaja o finansijskim događajima.
    
    Implementira:
    - 2 proste sekcije (filtriranje transakcija i penala)
    - 1 složenu sekciju (kompleksna analiza sa grafovima)
    
    Podrška za srpsku latinicu (Č, Ć, Đ, Š, Ž).
    """
    
    def __init__(self):
        self.reports_dir = "reports"
        if not os.path.exists(self.reports_dir):
            os.makedirs(self.reports_dir)
        
        # Registruj DejaVu fontove koji podržavaju srpsku latinicu
        self._register_fonts()
        
        self.styles = getSampleStyleSheet()
        self._setup_styles()
    
    def _register_fonts(self):
        """Registruje fontove koji podržavaju UTF-8 i srpsku latinicu"""
        try:
            from reportlab.pdfbase.ttfonts import TTFont
            from reportlab.pdfbase import pdfmetrics
            
            try:
                pdfmetrics.registerFont(TTFont('DejaVuSans', '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'))
                pdfmetrics.registerFont(TTFont('DejaVuSans-Bold', '/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf'))
                logger.info("DejaVu fontovi uspešno registrovani (sistemski)")
                self.font_name = 'DejaVuSans'
                self.font_name_bold = 'DejaVuSans-Bold'
            except:
                try:
                    # Pokušaj sa Windows putanjama
                    pdfmetrics.registerFont(TTFont('DejaVuSans', 'C:\\Windows\\Fonts\\DejaVuSans.ttf'))
                    pdfmetrics.registerFont(TTFont('DejaVuSans-Bold', 'C:\\Windows\\Fonts\\DejaVuSans-Bold.ttf'))
                    logger.info("DejaVu fontovi uspešno registrovani (Windows)")
                    self.font_name = 'DejaVuSans'
                    self.font_name_bold = 'DejaVuSans-Bold'
                except:
                    # Fallback: koristi Times-Roman koji ima bolju UTF-8 podršku od Helvetica
                    logger.warning("DejaVu fontovi nisu dostupni, koristim Times-Roman sa UTF-8")
                    self.font_name = 'Times-Roman'
                    self.font_name_bold = 'Times-Bold'
                
        except Exception as e:
            logger.warning(f"Problem sa registracijom fontova: {e}, koristim Times-Roman")
            self.font_name = 'Times-Roman'
            self.font_name_bold = 'Times-Bold'
    
    def _setup_styles(self):
        """Postavlja stilove za PDF dokument sa podrškom za srpsku latinicu"""

        self.styles.add(ParagraphStyle(
            name='CustomTitle',
            parent=self.styles['Heading1'],
            fontName=self.font_name_bold,
            fontSize=24,
            textColor=colors.HexColor('#1a1a1a'),
            spaceAfter=30,
            alignment=1,  # Centriran
            encoding='utf-8'
        ))
        
        # Podnaslov
        self.styles.add(ParagraphStyle(
            name='SectionTitle',
            parent=self.styles['Heading2'],
            fontName=self.font_name_bold,
            fontSize=16,
            textColor=colors.HexColor('#2c5aa0'),
            spaceAfter=12,
            spaceBefore=20,
            encoding='utf-8'
        ))
        
        # Opis sekcije
        self.styles.add(ParagraphStyle(
            name='SectionDescription',
            parent=self.styles['Normal'],
            fontName=self.font_name,
            fontSize=10,
            textColor=colors.HexColor('#555555'),
            spaceAfter=15,
            leftIndent=10,
            encoding='utf-8'
        ))
        
        # Normalni tekst
        self.styles['Normal'].fontName = self.font_name
        self.styles['Normal'].encoding = 'utf-8'
        
        # Heading3 - za podsekcije (3.1, 3.2, 3.3)
        self.styles['Heading3'].fontName = self.font_name_bold
        self.styles['Heading3'].encoding = 'utf-8'
        self.styles['Heading3'].fontSize = 14
        self.styles['Heading3'].spaceAfter = 10
        self.styles['Heading3'].spaceBefore = 15
    
    def _create_header(self) -> List:
        """Kreira zaglavlje izveštaja"""
        elements = []
        
        title = Paragraph("IZVEŠTAJ O FINANSIJSKIM DOGAĐAJIMA", self.styles['CustomTitle'])
        elements.append(title)
        
        timestamp = datetime.now().strftime("%d.%m.%Y %H:%M:%S")
        subtitle = Paragraph(f"Datum generisanja: {timestamp}", self.styles['Normal'])
        elements.append(subtitle)
        elements.append(Spacer(1, 0.5*cm))
        
        return elements
    
    def _create_simple_section_1(self, status_filter: Optional[str] = None, 
                                  min_iznos: Optional[float] = None,
                                  max_iznos: Optional[float] = None,
                                  limit: int = 50) -> List:
        """
        PROSTA SEKCIJA 1: Transakcije sa filterima
        
        Filtrira transakcije po statusu i iznosu.
        Prikazuje direktnu vizualizaciju slogova iz InfluxDB.
        """
        elements = []
        
        # Naslov sekcije
        title = Paragraph("1. PREGLED TRANSAKCIJA (Prosta sekcija)", self.styles['SectionTitle'])
        elements.append(title)
        
        # Opis
        filter_desc = f"Status: {status_filter or 'Svi'}, "
        filter_desc += f"Iznos: {min_iznos or 0} - {max_iznos or '∞'} RSD"
        desc = Paragraph(
            f"Prikaz transakcija sa filterima: {filter_desc}",
            self.styles['SectionDescription']
        )
        elements.append(desc)
        
        # Dohvatanje podataka
        try:
            transakcije = influx_service.get_dogadjaji_by_type("transakcija", limit=limit)
            
            # Filtriranje
            filtered = transakcije
            if status_filter:
                filtered = [t for t in filtered if t['status'] == status_filter]
            if min_iznos is not None:
                filtered = [t for t in filtered if t['iznos'] >= min_iznos]
            if max_iznos is not None:
                filtered = [t for t in filtered if t['iznos'] <= max_iznos]
            
            # Kreiranje tabele
            table_data = [
                ['Datum/Vreme', 'Faktura\nID', 'Status', 'Iznos\n(RSD)', 'Opis']
            ]
            
            for t in filtered[:limit]:
                timestamp_str = t['timestamp'].strftime("%d.%m.%Y\n%H:%M")
                table_data.append([
                    timestamp_str,
                    str(t['entitet_id']),
                    t['status'],
                    f"{t['iznos']:,.2f}",
                    t['opis'][:45] + '...' if len(t['opis']) > 45 else t['opis']
                ])
            
            # Stilizovanje tabele
            table = Table(table_data, colWidths=[3*cm, 1.5*cm, 2.5*cm, 2.5*cm, 8*cm])
            table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#2c5aa0')),
                ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
                ('VALIGN', (0, 0), (-1, -1), 'TOP'),
                ('FONTNAME', (0, 0), (-1, 0), self.font_name_bold),
                ('FONTSIZE', (0, 0), (-1, 0), 9),
                ('BOTTOMPADDING', (0, 0), (-1, 0), 8),
                ('TOPPADDING', (0, 0), (-1, 0), 8),
                ('LEFTPADDING', (0, 0), (-1, -1), 5),
                ('RIGHTPADDING', (0, 0), (-1, -1), 5),
                ('BACKGROUND', (0, 1), (-1, -1), colors.beige),
                ('GRID', (0, 0), (-1, -1), 0.5, colors.black),
                ('FONTNAME', (0, 1), (-1, -1), self.font_name),
                ('FONTSIZE', (0, 1), (-1, -1), 7),
                ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.lightgrey]),
                ('WORDWRAP', (0, 0), (-1, -1), True)
            ]))
            
            elements.append(table)
            
            # Statistika
            ukupan_iznos = sum(t['iznos'] for t in filtered)
            stats_text = f"<b>Ukupno pronađeno:</b> {len(filtered)} transakcija | <b>Ukupan iznos:</b> {ukupan_iznos:,.2f} RSD"
            elements.append(Spacer(1, 0.3*cm))
            elements.append(Paragraph(stats_text, self.styles['Normal']))
            
        except Exception as e:
            logger.error(f"Greška u prostoj sekciji 1: {str(e)}")
            error_msg = Paragraph(f"<font color='red'>Greška pri učitavanju podataka: {str(e)}</font>", 
                                 self.styles['Normal'])
            elements.append(error_msg)
        
        return elements
    
    def _create_simple_section_2(self, status_filter: Optional[str] = None,
                                  min_iznos: Optional[float] = None,
                                  limit: int = 50) -> List:
        """
        PROSTA SEKCIJA 2: Penali sa filterima
        
        Filtrira penale po statusu i iznosu.
        Prikazuje direktnu vizualizaciju slogova iz InfluxDB.
        """
        elements = []
        
        # Naslov sekcije
        title = Paragraph("2. PREGLED PENALA (Prosta sekcija)", self.styles['SectionTitle'])
        elements.append(title)
        
        # Opis
        filter_desc = f"Status: {status_filter or 'Svi'}, "
        filter_desc += f"Minimum iznos: {min_iznos or 0} RSD"
        desc = Paragraph(
            f"Prikaz penala sa filterima: {filter_desc}",
            self.styles['SectionDescription']
        )
        elements.append(desc)
        
        # Dohvatanje podataka
        try:
            penali = influx_service.get_dogadjaji_by_type("penal", limit=limit)
            
            # Filtriranje
            filtered = penali
            if status_filter:
                filtered = [p for p in filtered if p['status'] == status_filter]
            if min_iznos is not None:
                filtered = [p for p in filtered if p['iznos'] >= min_iznos]
            
            # Kreiranje tabele
            table_data = [
                ['Datum/Vreme', 'Ugovor\nID', 'Status', 'Iznos\n(RSD)', 'Razlog']
            ]
            
            for p in filtered[:limit]:
                timestamp_str = p['timestamp'].strftime("%d.%m.%Y\n%H:%M")
                table_data.append([
                    timestamp_str,
                    str(p['entitet_id']),
                    p['status'],
                    f"{p['iznos']:,.2f}",
                    p['opis'][:45] + '...' if len(p['opis']) > 45 else p['opis']
                ])
            
            # Stilizovanje tabele
            table = Table(table_data, colWidths=[3*cm, 1.5*cm, 2.5*cm, 2.5*cm, 8*cm])
            table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#c62828')),
                ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
                ('VALIGN', (0, 0), (-1, -1), 'TOP'),
                ('FONTNAME', (0, 0), (-1, 0), self.font_name_bold),
                ('FONTSIZE', (0, 0), (-1, 0), 9),
                ('BOTTOMPADDING', (0, 0), (-1, 0), 8),
                ('TOPPADDING', (0, 0), (-1, 0), 8),
                ('LEFTPADDING', (0, 0), (-1, -1), 5),
                ('RIGHTPADDING', (0, 0), (-1, -1), 5),
                ('BACKGROUND', (0, 1), (-1, -1), colors.beige),
                ('GRID', (0, 0), (-1, -1), 0.5, colors.black),
                ('FONTNAME', (0, 1), (-1, -1), self.font_name),
                ('FONTSIZE', (0, 1), (-1, -1), 7),
                ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.lightgrey]),
                ('WORDWRAP', (0, 0), (-1, -1), True)
            ]))
            
            elements.append(table)
            
            # Statistika
            ukupan_iznos = sum(p['iznos'] for p in filtered)
            stats_text = f"<b>Ukupno pronađeno:</b> {len(filtered)} penala | <b>Ukupan iznos:</b> {ukupan_iznos:,.2f} RSD"
            elements.append(Spacer(1, 0.3*cm))
            elements.append(Paragraph(stats_text, self.styles['Normal']))
            
        except Exception as e:
            logger.error(f"Greška u prostoj sekciji 2: {str(e)}")
            error_msg = Paragraph(f"<font color='red'>Greška pri učitavanju podataka: {str(e)}</font>", 
                                 self.styles['Normal'])
            elements.append(error_msg)
        
        return elements
    
    def _create_chart(self, data: List[Dict], chart_type: str = 'bar') -> Optional[str]:
        """
        Kreira grafikon i vraća putanju do slike
        
        Args:
            data: Podaci za grafikon
            chart_type: Tip grafikona ('bar', 'line', 'pie')
        """
        try:
            fig, ax = plt.subplots(figsize=(10, 6))
            
            if chart_type == 'dnevni_promet':
                # Grafikon za dnevni promet - ISPRAVLJENO sa pametnim grupisanjem datuma
                # Ograničimo na maksimalno 20 dana za čitljivost
                num_days = min(len(data), 20)
                
                # Sortiraj po datumu (najstariji prvi za grafikon)
                sorted_data = sorted(data[:num_days], key=lambda x: x['datum'])
                
                datumi = [d['datum'] for d in sorted_data]
                iznosi = [d['ukupan_iznos'] for d in sorted_data]
                
                ax.bar(datumi, iznosi, color='#2c5aa0', alpha=0.7)
                ax.set_xlabel('Datum', fontsize=10)
                ax.set_ylabel('Ukupan iznos (RSD)', fontsize=10)
                ax.set_title(f'Dnevni promet - Agregacija uspešnih transakcija (poslednjih {num_days} dana)', fontsize=12, fontweight='bold')
                
                # Pametno formatiranje x-ose - prikaži svaki n-ti datum za čitljivost
                if len(datumi) > 10:
                    # Ako ima više od 10 datuma, prikaži svaki drugi
                    step = 2
                    ax.set_xticks(range(0, len(datumi), step))
                    ax.set_xticklabels([datumi[i][-5:] for i in range(0, len(datumi), step)], rotation=45, ha='right', fontsize=8)  # Prikaži samo MM-DD
                else:
                    # Inače prikaži sve datume
                    plt.xticks(rotation=45, ha='right', fontsize=8)
                
                plt.yticks(fontsize=8)
                plt.grid(axis='y', alpha=0.3)
                
                # Dodaj formatiranje za y-osu (ako su veliki brojevi)
                ax.yaxis.set_major_formatter(plt.FuncFormatter(lambda x, p: f'{x:,.0f}'))
                
            elif chart_type == 'uporedna_analiza':
                # Grafikon za uporednu analizu
                nedelje = [d['nedelja'] for d in data[:12]]  # Poslednjih 12 nedelja
                penali = [d['broj_penala'] for d in data[:12]]
                transakcije = [d['broj_transakcija'] for d in data[:12]]
                
                x = range(len(nedelje))
                width = 0.35
                
                ax.bar([i - width/2 for i in x], penali, width, label='Penali', color='#c62828', alpha=0.7)
                ax.bar([i + width/2 for i in x], transakcije, width, label='Transakcije', color='#2c5aa0', alpha=0.7)
                
                ax.set_xlabel('Nedelja', fontsize=10)
                ax.set_ylabel('Broj događaja', fontsize=10)
                ax.set_title('Uporedna analiza - Penali vs Transakcije (nedeljno)', fontsize=12, fontweight='bold')
                ax.set_xticks(x)
                ax.set_xticklabels(nedelje, rotation=45, ha='right', fontsize=8)
                plt.yticks(fontsize=8)
                ax.legend()
                plt.grid(axis='y', alpha=0.3)
                
            elif chart_type == 'rizicni_penali':
                # Grafikon za rizične penale po ugovorima - ISPRAVLJENO
                # Koristimo podatke agregisane iz Flux upita (već grupisane po ugovoru)
                num_contracts = min(len(data), 10)  # Top 10 ugovora
                
                ugovor_ids = [f"Ugovor {p['entitet_id']}" for p in data[:num_contracts]]
                iznosi = [p['ukupan_iznos_po_ugovoru'] for p in data[:num_contracts]]
                
                # Obrnuti redosled za horizontalni grafikon (najmanji na dnu)
                ugovor_ids = ugovor_ids[::-1]
                iznosi = iznosi[::-1]
                
                ax.barh(ugovor_ids, iznosi, color='#c62828', alpha=0.7)
                ax.set_xlabel('Ukupan iznos penala (RSD)', fontsize=10)
                ax.set_ylabel('Ugovor ID', fontsize=10)
                ax.set_title(f'Top {num_contracts} ugovora sa najvećim penalima (> 5000 RSD)', fontsize=12, fontweight='bold')
                plt.xticks(fontsize=8)
                plt.yticks(fontsize=8)
                plt.grid(axis='x', alpha=0.3)
                
                # Dodaj formatiranje za x-osu
                ax.xaxis.set_major_formatter(plt.FuncFormatter(lambda x, p: f'{x:,.0f}'))
            
            plt.tight_layout()
            
            # Čuvanje grafikona
            chart_path = os.path.join(self.reports_dir, f'chart_{chart_type}_{datetime.now().timestamp()}.png')
            plt.savefig(chart_path, dpi=150, bbox_inches='tight')
            plt.close()
            
            return chart_path
            
        except Exception as e:
            logger.error(f"Greška pri kreiranju grafikona: {str(e)}")
            return None
    
    def _create_complex_section(self, days: int = 30, months: int = 3) -> List:
        """
        SLOŽENA SEKCIJA: Kompleksna analiza sa grafovima
        
        Kombinuje više upita:
        1. Dnevni promet sa agregatnom funkcijom (sum)
        2. Uporedna analiza (grupisanje po nedeljama)
        3. Rizični penali (filtriranje, agregacija, sortiranje)
        
        Uključuje grafikone za vizualizaciju.
        """
        elements = []
        
        # Naslov sekcije
        title = Paragraph("3. KOMPLEKSNA ANALIZA DOGAĐAJA (Složena sekcija)", self.styles['SectionTitle'])
        elements.append(title)
        
        desc = Paragraph(
            "Detaljna analiza obuhvata: dnevni promet sa agregatnom funkcijom sum(), "
            "uporednu analizu sa grupisanjem po nedeljama, i identifikaciju rizičnih penala "
            "sa složenim filterima i agregacijom po ugovorima.",
            self.styles['SectionDescription']
        )
        elements.append(desc)
        elements.append(Spacer(1, 0.3*cm))
        
        # ===== DIO 1: Dnevni promet =====
        try:
            dnevni_promet = influx_service.query_dnevni_promet(days)
            
            if dnevni_promet:
                # Podsekcija naslov
                subtitle1 = Paragraph("3.1 Dnevni promet (Agregacija sa SUM funkcijom)", self.styles['Heading3'])
                elements.append(subtitle1)
                
                desc1 = Paragraph(
                    "Flux upit: Agregacija iznosa uspešnih transakcija po danima korišćenjem sum() funkcije. "
                    "Podaci se grupišu po dnevnim intervalima (aggregateWindow) i sortiraju hronološki.",
                    self.styles['SectionDescription']
                )
                elements.append(desc1)
                elements.append(Spacer(1, 0.3*cm))
                
                # Grafikon
                chart_path = self._create_chart(dnevni_promet, 'dnevni_promet')
                if chart_path:
                    img = Image(chart_path, width=15*cm, height=9*cm)
                    elements.append(img)
                    elements.append(Spacer(1, 0.3*cm))
                
                # Statistika sa više detalja
                ukupan_promet = sum(d['ukupan_iznos'] for d in dnevni_promet)
                prosek = ukupan_promet / len(dnevni_promet) if dnevni_promet else 0
                max_dnevni = max(dnevni_promet, key=lambda x: x['ukupan_iznos']) if dnevni_promet else None
                min_dnevni = min(dnevni_promet, key=lambda x: x['ukupan_iznos']) if dnevni_promet else None
                
                stats_text = f"<b>Period analize:</b> {days} dana | "
                stats_text += f"<b>Ukupan promet:</b> {ukupan_promet:,.2f} RSD<br/>"
                stats_text += f"<b>Prosečan dnevni promet:</b> {prosek:,.2f} RSD | "
                
                if max_dnevni:
                    stats_text += f"<b>Maksimum:</b> {max_dnevni['ukupan_iznos']:,.2f} RSD ({max_dnevni['datum']})<br/>"
                if min_dnevni:
                    stats_text += f"<b>Minimum:</b> {min_dnevni['ukupan_iznos']:,.2f} RSD ({min_dnevni['datum']})"
                
                elements.append(Paragraph(stats_text, self.styles['Normal']))
                elements.append(Spacer(1, 0.5*cm))
        
        except Exception as e:
            logger.error(f"Greška u dnevnom prometu: {str(e)}")
            elements.append(Paragraph(f"<font color='red'>Greška: {str(e)}</font>", self.styles['Normal']))
        
        # ===== DIO 2: Uporedna analiza =====
        try:
            uporedna = influx_service.query_uporedna_analiza(months)
            
            if uporedna:
                # Podsekcija naslov
                subtitle2 = Paragraph("3.2 Uporedna analiza (Grupisanje po nedeljama)", self.styles['Heading3'])
                elements.append(subtitle2)
                
                desc2 = Paragraph(
                    "Flux upit: Grupisanje događaja po nedeljama sa COUNT agregacijom. "
                    "Kombinacija UNION operatora za paralelno procesiranje penala i transakcija.",
                    self.styles['SectionDescription']
                )
                elements.append(desc2)
                elements.append(Spacer(1, 0.3*cm))
                
                # Grafikon
                chart_path = self._create_chart(uporedna, 'uporedna_analiza')
                if chart_path:
                    img = Image(chart_path, width=15*cm, height=9*cm)
                    elements.append(img)
                    elements.append(Spacer(1, 0.3*cm))
                
                # Statistika sa detaljnijom analizom
                ukupno_penala = sum(d['broj_penala'] for d in uporedna)
                ukupno_transakcija = sum(d['broj_transakcija'] for d in uporedna)
                broj_nedelja = len(uporedna)
                prosek_penala = ukupno_penala / broj_nedelja if broj_nedelja else 0
                prosek_transakcija = ukupno_transakcija / broj_nedelja if broj_nedelja else 0
                
                stats_text = f"<b>Period analize:</b> {months} mesec(i), {broj_nedelja} nedelja<br/>"
                stats_text += f"<b>Ukupno penala:</b> {ukupno_penala} | "
                stats_text += f"<b>Prosečno nedeljno:</b> {prosek_penala:.1f}<br/>"
                stats_text += f"<b>Ukupno transakcija:</b> {ukupno_transakcija} | "
                stats_text += f"<b>Prosečno nedeljno:</b> {prosek_transakcija:.1f}"
                
                elements.append(Paragraph(stats_text, self.styles['Normal']))
                elements.append(Spacer(1, 0.5*cm))
        
        except Exception as e:
            logger.error(f"Greška u uporednoj analizi: {str(e)}")
            elements.append(Paragraph(f"<font color='red'>Greška: {str(e)}</font>", self.styles['Normal']))
        
        # ===== DIO 3: Rizični penali =====
        try:
            rizicni = influx_service.query_rizicni_penali(min_iznos=5000, limit=10)
            
            if rizicni:
                # PAGEBREAK da tabela ne prelazi na drugu stranu
                elements.append(PageBreak())
                
                # Podsekcija naslov
                subtitle3 = Paragraph("3.3 Rizični penali (Filtriranje, agregacija i sortiranje)", self.styles['Heading3'])
                elements.append(subtitle3)
                
                desc3 = Paragraph(
                    "Flux upit: Kompleksna analiza sa PIVOT, FILTER (iznos > 5000 RSD), GROUP BY (entitet_id), "
                    "REDUCE agregacijom za sumiranje iznosa i brojanje penala po ugovoru, i SORT DESC po ukupnom iznosu.",
                    self.styles['SectionDescription']
                )
                elements.append(desc3)
                elements.append(Spacer(1, 0.3*cm))
                
                # Grafikon
                chart_path = self._create_chart(rizicni, 'rizicni_penali')
                if chart_path:
                    img = Image(chart_path, width=15*cm, height=9*cm)
                    elements.append(img)
                    elements.append(Spacer(1, 0.3*cm))
                
                # Tabela sa detaljima - ISPRAVLJENO i optimizovano
                # Podaci iz query_rizicni_penali već su agregisani po ugovoru
                table_data = [
                    ['Ugovor\nID', 'Ukupan iznos\n(RSD)', 'Broj\npenala', 'Poslednji razlog', 'Datum']
                ]
                
                for r in rizicni[:10]:
                    # Formatiranje datuma
                    datum_str = "N/A"
                    if 'poslednje_vreme' in r and r['poslednje_vreme']:
                        try:
                            datum_str = r['poslednje_vreme'].strftime("%d.%m.%Y")
                        except:
                            datum_str = str(r['poslednje_vreme'])[:10]
                    
                    table_data.append([
                        str(r['entitet_id']),
                        f"{r['ukupan_iznos_po_ugovoru']:,.2f}",
                        str(r['broj_penala_po_ugovoru']),
                        r['poslednji_opis'][:35] + '...' if len(r['poslednji_opis']) > 35 else r['poslednji_opis'],
                        datum_str
                    ])
                
                # Optimizovane širine kolona - ukupno 17.5cm (unutar margina)
                table = Table(table_data, colWidths=[1.8*cm, 2.8*cm, 1.5*cm, 9*cm, 2.4*cm])
                table.setStyle(TableStyle([
                    ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#c62828')),
                    ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                    ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
                    ('VALIGN', (0, 0), (-1, -1), 'TOP'),
                    ('FONTNAME', (0, 0), (-1, 0), self.font_name_bold),
                    ('FONTSIZE', (0, 0), (-1, 0), 8),
                    ('BOTTOMPADDING', (0, 0), (-1, 0), 8),
                    ('TOPPADDING', (0, 0), (-1, 0), 8),
                    ('LEFTPADDING', (0, 0), (-1, -1), 5),
                    ('RIGHTPADDING', (0, 0), (-1, -1), 5),
                    ('GRID', (0, 0), (-1, -1), 0.5, colors.black),
                    ('FONTNAME', (0, 1), (-1, -1), self.font_name),
                    ('FONTSIZE', (0, 1), (-1, -1), 7),
                    ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.lightgrey]),
                    ('WORDWRAP', (0, 0), (-1, -1), True)
                ]))
                
                elements.append(table)
                elements.append(Spacer(1, 0.3*cm))
                
                # Statistika - DODATO
                ukupan_iznos_rizicnih = sum(r['ukupan_iznos_po_ugovoru'] for r in rizicni)
                ukupan_broj_penala = sum(r['broj_penala_po_ugovoru'] for r in rizicni)
                broj_ugovora = len(rizicni)
                
                stats_text = f"<b>Broj rizičnih ugovora:</b> {broj_ugovora} | "
                stats_text += f"<b>Ukupan iznos penala:</b> {ukupan_iznos_rizicnih:,.2f} RSD | "
                stats_text += f"<b>Ukupan broj penala:</b> {ukupan_broj_penala}"
                
                elements.append(Paragraph(stats_text, self.styles['Normal']))
        
        except Exception as e:
            logger.error(f"Greška u rizičnim penalima: {str(e)}")
            elements.append(Paragraph(f"<font color='red'>Greška: {str(e)}</font>", self.styles['Normal']))
        
        return elements
    
    def generate_full_report(
        self,
        # Parametri za prostu sekciju 1
        transakcije_status: Optional[str] = None,
        transakcije_min_iznos: Optional[float] = None,
        transakcije_max_iznos: Optional[float] = None,
        # Parametri za prostu sekciju 2
        penali_status: Optional[str] = None,
        penali_min_iznos: Optional[float] = None,
        # Parametri za složenu sekciju
        dnevni_promet_days: int = 30,
        uporedna_analiza_months: int = 3,
        # Opšti parametri
        limit: int = 50
    ) -> str:
        """
        Generiše kompletan PDF izveštaj sa svim sekcijama
        
        Returns:
            Putanja do generisanog PDF fajla
        """
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        pdf_filename = f"izvestaj_finansijski_dogadjaji_{timestamp}.pdf"
        pdf_path = os.path.join(self.reports_dir, pdf_filename)
        
        # Kreiranje PDF dokumenta
        doc = SimpleDocTemplate(
            pdf_path,
            pagesize=A4,
            rightMargin=2*cm,
            leftMargin=2*cm,
            topMargin=2*cm,
            bottomMargin=2*cm
        )
        
        elements = []
        
        # Zaglavlje
        elements.extend(self._create_header())
        elements.append(Spacer(1, 0.5*cm))
        
        # Prosta sekcija 1: Transakcije
        elements.extend(self._create_simple_section_1(
            status_filter=transakcije_status,
            min_iznos=transakcije_min_iznos,
            max_iznos=transakcije_max_iznos,
            limit=limit
        ))
        elements.append(Spacer(1, 1*cm))
        
        # Prosta sekcija 2: Penali
        elements.extend(self._create_simple_section_2(
            status_filter=penali_status,
            min_iznos=penali_min_iznos,
            limit=limit
        ))
        elements.append(PageBreak())
        
        # Složena sekcija: Kompleksna analiza
        elements.extend(self._create_complex_section(
            days=dnevni_promet_days,
            months=uporedna_analiza_months
        ))
        
        # Footer
        elements.append(Spacer(1, 1*cm))
        footer = Paragraph(
            "<i>Izveštaj generisan automatski od strane FinancialEventsAnalysisService</i>",
            self.styles['Normal']
        )
        elements.append(footer)
        
        # Generisanje PDF-a
        doc.build(elements)
        
        logger.info(f"PDF izveštaj uspešno generisan: {pdf_path}")
        return pdf_path


# Singleton instanca
report_service = ReportService()
