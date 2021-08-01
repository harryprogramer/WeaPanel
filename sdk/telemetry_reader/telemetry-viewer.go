package main

import "C"
import (
	"bufio"
	"flag"
	"fmt"
	"github.com/dariubs/percent"
	"github.com/golang/protobuf/proto"
	"io/ioutil"
	"log"
	"os"
	"strconv"
	"syscall"
	"time"
	"unsafe"
)

type FormatterOptions struct {
	startChar 	byte
	stopChar	byte
	spaceChar 	byte
}

type SHORT uint16

type COORD struct {
	X	SHORT
	Y 	SHORT
}

var (
	dll = syscall.MustLoadDLL("C:\\Users\\szymo\\go\\src\\WeaInternalSDK\\win32console.dll")
	conXCall = dll.MustFindProc("_Z11getConsoleXv")
	conYCall = dll.MustFindProc( "_Z11getConsoleYv")
	conCursorXCall = dll.MustFindProc("_Z17GetConsoleCursorXv")
	conCursorYCall = dll.MustFindProc("_Z17GetConsoleCursorYv")
	setConCursorPositionCall = dll.MustFindProc("_Z24SetConsoleCursorPositionss")
)

func isDoubleClickRun() bool {
	kernel32 := syscall.NewLazyDLL("kernel32.dll")
	lp := kernel32.NewProc("GetConsoleProcessList")
	if lp != nil {
		var pids [2]uint32
		var maxCount uint32 = 2
		ret, _, _ := lp.Call(uintptr(unsafe.Pointer(&pids)), uintptr(maxCount))
		if ret > 1 {
			return false
		}
	}
	return true
}

//getConsoleX_Win
func getConsoleX() int {
	conX, _, _ := conXCall.Call(1)
	return int(conX) - 7
}

func getConsoleY() int {
	conY, _, _ := conYCall.Call(1)
	return int(conY)
}

func getConsoleCursorX() int {
	conX, _, _ := conCursorXCall.Call(1)
	return int(conX)
}

func getConsoleCursorY() int {
	conY, _, _ := conCursorYCall.Call(1)
	return int(conY)
}

func setConsoleCursorPosition(coord COORD){
	_, u, err := setConCursorPositionCall.Call(1, uintptr(coord.X), uintptr(coord.Y))
	if err != nil {
		log.Fatalln(u)
	}
}


func printDetails(name string, value string, size int){
	options := FormatterOptions{
		startChar: '|',
		stopChar: '|',
		spaceChar: ' ',
	}
	printDetailsF(name, value, size, options)
}


func printDetailsF(name string, value string, size int, options FormatterOptions){
	if size < (len(name) + len(value) + 5){
		panic("Size of buffer " + strconv.FormatInt(int64(size), 10) + " is too small for all text")
	}
	spacesLength := size - (len(name) + len(value) + 5)

	var spaces []byte

	for i := 0; i < spacesLength; i++ {
		spaces = append(spaces, options.spaceChar)
	}

	fmt.Println(
		string(options.startChar) +
			string(options.spaceChar) +
			name + ":" +
			string(spaces) +
			value +
			string(options.spaceChar) +
			string(options.stopChar))
}

func centerText(size int, text string){
	options := FormatterOptions{
		startChar: '|',
		stopChar: '|',
		spaceChar: ' ',
	}
	centerTextF(size, text, options)
}

func centerTextF(size int, text string, options FormatterOptions) {
	size = size - 2
	if len(text) > size {
		panic("Text length is bigger than size")
	}

	if len(text) % 2 != 0 {
		panic("Rozmiar tekstu musi być parzysty")
	}

	if size % 2 != 0 || size == 0x0 {
		panic("Rozmiar centrowania musi być parzysty i nie może być zerem")
	}

	spaceChar := options.spaceChar
	var finalString string
	textLength := size / 2 - (len(text) / 2)

	for i := 0; i < 2; i++ {
		if i == 0 {
			finalString += string(options.startChar)
		}
		for l := 0; l < textLength ; l++ {
			finalString += string(spaceChar)
		}
		if len(text) != 0{
			finalString += text; text = ""
		}

		if i == 1 {
			finalString += string(options.stopChar)
		}
	}

	fmt.Println(finalString)
}

func formatDate(date *Date) string{
	day := strconv.FormatInt(int64(date.GetDay()), 10)
	month := strconv.FormatInt(int64(date.GetMonth()), 10)

	if !(date.GetDay() > 10) {
		if !(date.GetDay() == 10) {
			day = "0" + strconv.FormatInt(int64(date.GetDay()), 10)
		}
	}

	if !(date.GetMonth() > 10){
		if !(date.GetMonth() == 10) {
			month = "0" + strconv.FormatInt(int64(date.GetMonth()), 10)
		}
	}

	t, _ := time.Parse( "01-02-2006", month + "-" + day + "-" + strconv.FormatInt(int64(date.GetYear()), 10))

	var dayOfWeek string

	switch t.Weekday() {
	case time.Monday: {
		dayOfWeek = "Poniedzialek"
	}
	case time.Tuesday: {
		dayOfWeek = "Wtorek"
	}
	case time.Wednesday: {
		dayOfWeek = "Sroda"
	}
	case time.Thursday: {
		dayOfWeek = "Czwartek"
	}
	case time.Friday: {
		dayOfWeek = "Piatek"
	}
	case time.Saturday: {
		dayOfWeek = "Sobota"
	}
	case time.Sunday: {
		dayOfWeek = "Niedziela"
	}
	default:
		dayOfWeek = "Unknown"

	}

	var monthName string

	switch t.Month() {
	case 1: {
		monthName = "Styczen"
	}
	case 2: {
		monthName = "Luty"
	}
	case 3: {
		monthName = "Marzec" // kekw
	}
	case 4: {
		monthName = "Kwiecien"
	}
	case 5: {
		monthName = "Maj"
	}
	case 6: {
		monthName = "Czerwiec"
	}
	case 7: {
		monthName = "Lipiec"
	}
	case 8: {
		monthName = "Sierpien"
	}
	case 9: {
		monthName = "Wrzesien"
	}
	case 10: {
		monthName = "Październik"
	}
	case 11: {
		monthName = "Listopad"
	}
	case 12: {
		monthName = "Grudzien"
	}
	default:
		monthName = "Unknown"
	}

	var hour string
	var minutes string
	var seconds string

	hour = strconv.FormatInt(int64(date.GetHours()), 10)
	minutes = strconv.FormatInt(int64(date.GetMinutes()), 10)
	seconds = strconv.FormatInt(int64(date.GetSeconds()), 10)

	if !(date.GetHours() > 10){
		if date.GetHours() != 10 {
			hour = "0" + strconv.FormatInt(int64(date.GetHours()), 10)
		}
	}

	if !(date.GetMinutes() > 10){
		if date.GetMinutes() != 0 {
			minutes = "0" + strconv.FormatInt(int64(date.GetMinutes()), 10)
		}
	}

	if !(date.GetSeconds() > 10){
		if date.GetSeconds() != 0 {
			seconds = "0" + strconv.FormatInt(int64(date.GetSeconds()), 10)
		}
	}

	return dayOfWeek[:3] + " " + monthName + " " +
		strconv.FormatInt(int64(date.GetDay()), 10) + " " +
		strconv.FormatInt(int64(date.GetYear()), 10) + " " +
		hour + ":" + minutes + ":" + seconds + "," +
		strconv.FormatInt(int64(date.GetMillis()), 10)[:2] + " GMT+2"
}

func printData(telemetry *Telemetry, filename string){
	calculatePages := func(number uint64) uint64 {
		if number < 5 {
			return 1
		}
		var pagesNumber uint64 = 0
		if number % 5 != 0 {
			number = number - number % 5
			pagesNumber++
		}

		for !(number == 0) {
			number = number - 5
			pagesNumber++
		}

		return pagesNumber
	}
	log.Println("Formating data...")
	log.Println("Console size is: " + strconv.FormatInt(int64(getConsoleX()), 10) + "x" + strconv.FormatInt(int64(getConsoleY()), 10))
	size := int(percent.Percent(60, getConsoleX()))
	var pagesCount int
	if size < 10 || size > 500{
		size = 120 // windows default console size
	}

	if size < 89 {
		size = (89 - size) + size
	}

	if size % 2 != 0 {
		size = size - 1
	}

	if len(filename) > 20 {
		filename = filename[:17] + "..."
	}


	log.Println("Building console GUI...")

	pages := calculatePages(uint64(len(telemetry.GetPacket())))

	fmt.Printf("\n\n\n")
	var header []byte

	header = append(header, '|')
	for i := 0; i < size - 2; i++ {
		header = append(header, '-')
	}
	header = append(header, '|')

	fmt.Println(string(header))
	if len(filename) % 2 == 0{
		filename = filename[1:] + ".."
	}
	centerText(size, "Telemetry data " + filename)
	fmt.Println(string(header))
	formatDate(telemetry.Header.StartTime)
	printDetails("Date", formatDate(telemetry.Header.StartTime), size)
	printDetails("Data Captured", strconv.FormatInt(int64(len(telemetry.GetPacket())), 10), size)
	printDetails("Probe IP", telemetry.GetHeader().GetStationIp(), size)
	printDetails("Probe MAC", telemetry.GetHeader().GetStationMac(), size)
	printDetails("Probe DNS", telemetry.GetHeader().GetStationDns(), size)
	printDetails("Probe Gateway", telemetry.GetHeader().GetStationGateway(), size)
	printDetails("Probe UUID", telemetry.GetHeader().GetStationUuid(), size)
	printDetails("Probe System Version", telemetry.GetHeader().GetSysVersion(), size)
	printDetails("Server Version", telemetry.GetHeader().GetServerVersion(), size)
	printDetails("File Version", telemetry.GetHeader().GetTelemetryPluginVer(), size)
	formatOptions := FormatterOptions{
		startChar: '|',
		stopChar: '|',
		spaceChar: '=',
	}
	// ============== PAGES ====================
	centerTextF(size, "", formatOptions)
	consoleCursorAreaStart := getConsoleCursorY()
	if pagesCount == 1 {
		for i := 0; i < len(telemetry.GetPacket()); i++ {
			printDetails("Date", formatDate(telemetry.GetPacket()[i].Date), size)
			printDetails("Temperature", fmt.Sprint(telemetry.GetPacket()[i].Temperature), size)
			fmt.Println(string(header))
			syscall.Exit(1)
		}
	}


	for i := 0; i < 5; i++ {
		printDetails("Date", formatDate(telemetry.GetPacket()[i].Date), size)
		printDetails("Temperature", fmt.Sprint(telemetry.GetPacket()[i].Temperature), size)
		fmt.Println(string(header))
	}

	centerTextF(size, "", formatOptions)
	textPage := "Page [1] of " + strconv.FormatInt(int64(pages), 10)
	if len(textPage) % 2 != 0 {
		textPage += " "
	}
	centerText(size, textPage)
	centerTextF(size, "", formatOptions)

	consoleCursorAreaEnd := getConsoleCursorY()

	fmt.Println(consoleCursorAreaStart)
	fmt.Println(consoleCursorAreaEnd)

	for ;; {
		scanner := bufio.NewScanner(os.Stdin)
		for scanner.Scan() {

			coord := COORD{
				X: SHORT(consoleCursorAreaEnd - consoleCursorAreaStart),
				Y: 0,
			}

			fmt.Println(scanner.Text())
			setConsoleCursorPosition(coord)
		}
	}
}

func main() {
	fmt.Println(getConsoleCursorY())
	if isDoubleClickRun() {
		fmt.Println("This is a command line tool\n\nYou must open this file with cmd.exe")
		var b byte
		_, _ = fmt.Scanf("%v", &b)
		syscall.Exit(-1)
	}

	var fileName string

	flag.StringVar(&fileName, "file", "", "Lokalizacja pliku telemetrii")
	flag.Parse()
	if len(fileName) == 0 {
		log.Fatalln("Brak nazwy pliku")
	}
	log.Println("Loading file " + fileName)
	in, err := ioutil.ReadFile(fileName)
	if err != nil {
		log.Fatalln("Error reading file:", err)
	}
	log.Println("Reading file " + fileName)
	telemetry := &Telemetry{}
	buffer := proto.NewBuffer(in)
	if err := buffer.DecodeMessage(telemetry); err != nil {
		log.Fatalln("Failed to load telemetry file:", err)
	}
	printData(telemetry, fileName)

}
